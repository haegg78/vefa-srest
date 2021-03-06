package no.sr.ringo.http;

import com.google.inject.Inject;
import no.sr.ringo.ObjectMother;
import no.sr.ringo.account.Account;
import no.sr.ringo.client.Message;
import no.sr.ringo.client.Messagebox;
import no.sr.ringo.client.Messages;
import no.sr.ringo.guice.ServerTestModuleFactory;
import no.sr.ringo.message.ReceptionId;
import no.sr.ringo.persistence.DbmsTestHelper;
import no.sr.ringo.persistence.jdbc.util.DatabaseHelper;
import no.sr.ringo.transport.TransferDirection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

// The test database will be initialized with initial scheme
@Guice(moduleFactory = ServerTestModuleFactory.class)
public class MessagesIntegrationTest extends AbstractHttpClientServerTest {

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    DbmsTestHelper dbmsTestHelper;

    private List<Long> messageIds = new ArrayList<>();

    /**
     * Retrieves all messages from both inbox and outbox
     *
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Test(groups = {"integration"})
    public void count() throws IOException, URISyntaxException {

        Messagebox messagebox = ringoRestClientImpl.getMessageBox();

        Integer count = messagebox.getCount();
        assertNotNull(count);

        assertTrue(count >= 0);
    }

    @Test(groups = {"integration"})
    public void testGetContentsOfMessages() throws Exception {

        Messagebox messagebox = ringoRestClientImpl.getMessageBox();
        final Messages messages = messagebox.getMessages();
        int count = 0;
        for (Message message : messages) {
            count++;
        }
        assertTrue(count>0,"Expected more than 0 messages");
    }


    @Test(groups = {"slow", "integration"})
    public void testIteration() throws Exception {
        //We have inserted 27 messages in the setup
        Messagebox messagebox = ringoRestClientImpl.getMessageBox();

        //fetches the messages object which is iterable
        final Messages messages = messagebox.getMessages();

        int count = 0;
        //iterates all the messages
        for (Message message : messages) {
            count++;
        }

        //checks that we have more than 26 messages
        assertTrue(count>26,"Expected more than 26 messages");

    }

    @BeforeMethod(groups = {"slow"})
    public void insertSample() throws SQLException {
        final Account account = ObjectMother.getTestAccount();
        for (int i = 0; i <= 26; i++) {
            final Long message = dbmsTestHelper.createSampleMessage(account.getAccountId().toInteger(), TransferDirection.IN, ObjectMother.getTestParticipantIdForSMPLookup().getIdentifier(), ObjectMother.getTestParticipantIdForSMPLookup().getIdentifier(), new ReceptionId(), null);
            messageIds.add(message);
        }
    }

    @AfterMethod(groups = {"slow"})
    public void deleteSample() throws SQLException {
        for (Long messageId : messageIds) {
            databaseHelper.deleteMessage(messageId);
        }
    }

}

