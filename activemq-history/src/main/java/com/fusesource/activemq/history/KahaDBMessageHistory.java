package com.fusesource.activemq.history;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Message;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.store.kahadb.JournalCommand;
import org.apache.activemq.store.kahadb.MessageDatabase;
import org.apache.activemq.store.kahadb.data.KahaAddMessageCommand;
import org.apache.activemq.store.kahadb.data.KahaCommitCommand;
import org.apache.activemq.store.kahadb.data.KahaRemoveMessageCommand;
import org.apache.activemq.store.kahadb.data.KahaRollbackCommand;
import org.apache.activemq.store.kahadb.data.KahaTransactionInfo;
import org.apache.kahadb.journal.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KahaDBMessageHistory extends MessageHistory {

    private static final Logger log = LoggerFactory.getLogger(KahaDBMessageHistory.class);
    private MessageDatabase manager = null;

    public void run() throws Exception {
        try {
            manager = new MessageDatabase();
            manager.setDirectory(journalDirs.get(0));
            manager.getJournal().setFilePrefix("db-");
            manager.getJournal().start();

            scanReporter.started();
            Map<String, IndividualMessageHistory> matches = findMatchingMessages();
            scanReporter.done();

//      moveReporter.started(matches.size());
            logMatchingMessages(matches);
//     
            manager.close();

//      moveReporter.done();
        } catch (Exception ex) {
            log.error("Unable to create target broker; details: ", ex);
            throw new Exception("Unable to create target broker; details: " + ex.getMessage());
        }
    }

    private void logMatchingMessages(Map<String, IndividualMessageHistory> msgs)
            throws Exception {
        Map<String, Location> processedMsgs = new HashMap<String, Location>();

        Location curr = manager.getJournal().getNextLocation(null);

        while (curr != null) {

            JournalCommand cmd = (JournalCommand) manager.load(curr);

            if (cmd instanceof KahaAddMessageCommand) {
                KahaAddMessageCommand addMsg = (KahaAddMessageCommand) cmd;
                String q = addMsg.getDestination().getName();
                String msgId = addMsg.getMessageId();

                if (queue == null || q.equals(queue)) {
                    Message msg = (Message) wireFormat.unmarshal(new DataInputStream(addMsg.getMessage().newInput()));
                    long ts = msg.getJMSTimestamp();
                    if (isInTimeWindow(ts)) {
                        msgs.get(msgId).receivedAt = ts;
                        ActiveMQDestination dst = ((ActiveMQMessage) msg).getDestination();
                        String queue = dst.getPhysicalName().toString();

                        logMessage(log, msg, curr.getSize(), msgs.get(msgId));

//                    moveReporter.movedMessage(queue, ts);

                        processedMsgs.put(msgId, curr);
                    }
                }
            }

            curr = manager.getJournal().getNextLocation(curr);
        }

//        moveReporter.summarize(broker.getStoreDirectory().getAbsolutePath());
    }

    public boolean isInTimeWindow(long t) {
        boolean ret = false;

        if (from == 0 && to == 0) {
            ret = true;
        } else if (from == 0 && to != 0) {
            ret = t <= to;
        } else if (from != 0 && to == 0) {
            ret = t >= from;
        } else if (from != 0 && to != 0) {
            ret = t >= from && t <= to;
        }

        return ret;
    }

    private boolean isNotTransacted(KahaTransactionInfo tx) {
        return tx.getLocalTransacitonId().toString().isEmpty()
                && tx.getXaTransacitonId().toString().isEmpty();
    }

    private String getTransactionKey(KahaTransactionInfo tx) {
        return tx.getLocalTransacitonId().toString()
                + tx.getXaTransacitonId().toString();
    }

    public class IndividualMessageHistory {

        public long receivedAt;
        public long acknowledgedAt;
        public boolean acknowledged;
    }

    private Map<String, IndividualMessageHistory> findMatchingMessages() throws Exception {
        // Set<String> msgIds = new HashSet<String>();
        Map<String, List<JournalCommand>> transactedCmds = new HashMap<String, List<JournalCommand>>();
        Map<String, IndividualMessageHistory> msgs = new HashMap<String, IndividualMessageHistory>();

        int numCommands = 0;
        int currentFileId = 0;

        Location curr = manager.getJournal().getNextLocation(null);
        while (curr != null) {
            // If we've begun processing a new file.
            if (currentFileId != curr.getDataFileId()) {
                currentFileId = curr.getDataFileId();
                scanReporter.newJournalFile(currentFileId + "");
            }

            JournalCommand cmd = (JournalCommand) manager.load(curr);
            if (cmd instanceof KahaAddMessageCommand) {
                KahaAddMessageCommand addMsg = (KahaAddMessageCommand) cmd;

                if (isNotTransacted(addMsg.getTransactionInfo())) {
                    // not transacted.
                    log.debug("AddMessage " + addMsg.getMessageId());
                    addMessageForQueue(msgs, addMsg.getMessageId(), addMsg.getDestination().getName());
                } else {
                    // transacted. Wait till a commit or rollback before adding.
                    String tx = getTransactionKey(addMsg.getTransactionInfo());
                    log.debug("AddMessage " + addMsg.getMessageId() + ", txnId = " + tx);
                    List<JournalCommand> txCmds = transactedCmds.get(tx);
                    if (txCmds == null) {
                        txCmds = new ArrayList<JournalCommand>();
                        transactedCmds.put(tx, txCmds);
                    }
                    txCmds.add(addMsg);
                }

                scanReporter.addMessage();
            } else if (cmd instanceof KahaRemoveMessageCommand) {
                KahaRemoveMessageCommand rmMsg = (KahaRemoveMessageCommand) cmd;

                if (isNotTransacted(rmMsg.getTransactionInfo())) {
                    log.debug("RemoveMessage " + rmMsg.getMessageId());
                    markMessageAsAcknowledged(msgs, rmMsg.getMessageId(), rmMsg.getDestination().getName());
                } else {
                    // transacted. 
                    String tx = getTransactionKey(rmMsg.getTransactionInfo());
                    log.debug("Transactional RemoveMessage " + rmMsg.getMessageId() + ", txnId = " + tx);

                    List<JournalCommand> txCmds = transactedCmds.get(tx);
                    if (txCmds == null) {
                        txCmds = new ArrayList<JournalCommand>();
                        transactedCmds.put(tx, txCmds);
                    }
                    txCmds.add(rmMsg);
                }

                scanReporter.removeMessage();
            } else if (cmd instanceof KahaRollbackCommand) {
                KahaRollbackCommand rb = (KahaRollbackCommand) cmd;
                String tx = getTransactionKey(rb.getTransactionInfo());
                log.debug("Rollback, id " + tx);
                transactedCmds.remove(tx);
                scanReporter.rollbackCommand();
            } else if (cmd instanceof KahaCommitCommand) {
                KahaCommitCommand c = (KahaCommitCommand) cmd;
                String tx = getTransactionKey(c.getTransactionInfo());
                log.debug("Commit, id " + tx);
                if (transactedCmds.containsKey(tx)) {
                    for (JournalCommand jc : transactedCmds.get(tx)) {
                        if (jc instanceof KahaAddMessageCommand) {
                            KahaAddMessageCommand addMsg = (KahaAddMessageCommand) jc;
                            addMessageForQueue(msgs, addMsg.getMessageId(), addMsg.getDestination().getName());
                        } else if (jc instanceof KahaRemoveMessageCommand) {
                            KahaRemoveMessageCommand rmMsg = (KahaRemoveMessageCommand) jc;
                            markMessageAsAcknowledged(msgs, rmMsg.getMessageId(), rmMsg.getDestination().getName());
                        }
                    }
                }
                scanReporter.commitCommand();
            } else {
                log.debug("Command " + cmd.getClass().getSimpleName());
            }

            numCommands++;

            curr = manager.getJournal().getNextLocation(curr);

            // report on the number of undelivered messages. 
            //


            if (curr == null || currentFileId != curr.getDataFileId()) {
                scanReporter.endOfJournal(undelivered(msgs));
            }

        }

        scanReporter.endOfScan(numCommands, msgs.size() - undelivered(msgs), undelivered(msgs));

        return msgs;
    }

    public int undelivered(Map<String, IndividualMessageHistory> msgs) {
        int undelivered = 0;
        for (String id : msgs.keySet()) {
            if (!msgs.get(id).acknowledged) {
                undelivered++;
            }
        }
        return undelivered;
    }

    public void addMessageForQueue(Map<String, IndividualMessageHistory> msgs, String msgId, String q) {
        // Only add messages that we're interested in.         
        //
        if (queue == null || q.equals(queue)) {

            IndividualMessageHistory h = new IndividualMessageHistory();
            h.acknowledged = false;
            msgs.put(msgId, h);
        }
    }

    public void markMessageAsAcknowledged(Map<String, IndividualMessageHistory> msgs, String msgId, String q) {

        if (queue == null || q.equals(queue)) {
            if (msgs.containsKey(msgId)) {
                IndividualMessageHistory h = msgs.get(msgId);
                h.acknowledged = true;
            }

        }
    }
}
