package no.sr.ringo.usecase;

import com.google.inject.Inject;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.sr.ringo.ObjectMother;
import no.sr.ringo.email.EmailService;
import no.sr.ringo.guice.ServerTestModuleFactory;
import no.sr.ringo.message.MessageMetaData;
import no.sr.ringo.message.MessageWithLocations;
import no.sr.ringo.message.OutboundPostParams;
import no.sr.ringo.message.PeppolMessageRepository;
import no.sr.ringo.peppol.PeppolChannelId;
import no.sr.ringo.peppol.PeppolDocumentTypeId;
import no.sr.ringo.peppol.PeppolProcessIdAcronym;
import no.sr.ringo.persistence.jdbc.util.DatabaseHelper;
import no.sr.ringo.persistence.queue.OutboundMessageQueueState;
import no.sr.ringo.persistence.queue.QueueRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the use case of receiving an outboundMesssageMetaData message from a client.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
@Guice(moduleFactory = ServerTestModuleFactory.class)
public class ReceiveMessageFromClientUseCaseIntegrationTest {

    private static final String EHF_TEST_SEND_REGNING_HELSE_VEST2_XML = "ehf-test-sendregning.xml";

    private ParticipantIdentifier participantId;
    private final DatabaseHelper databaseHelper;
    private final PeppolMessageRepository peppolMessageRepository;


    private final ReceiveMessageFromClientUseCase receiveMessageFromClientUseCase;

    @Inject
    public ReceiveMessageFromClientUseCaseIntegrationTest(DatabaseHelper databaseHelper, QueueRepository queueRepository, PeppolMessageRepository peppolMessageRepository, EmailService emailService) {
        this.databaseHelper = databaseHelper;
        this.peppolMessageRepository = peppolMessageRepository;
        this.receiveMessageFromClientUseCase = new ReceiveMessageFromClientUseCase(ObjectMother.getTestAccount(), peppolMessageRepository, queueRepository, emailService);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        participantId = ObjectMother.getTestParticipantIdForSMPLookup();
    }

    @Test(groups = {"persistence"})
    public void saveAndQueueMessage() throws ParserConfigurationException, IOException, SAXException {

        // Retrieves the sample XML document, parses it and shoves it into the message.
        InputStream is = ReceiveMessageFromClientUseCaseIntegrationTest.class.getClassLoader().getResourceAsStream(EHF_TEST_SEND_REGNING_HELSE_VEST2_XML);
        assertNotNull(is, "Unable to find " + EHF_TEST_SEND_REGNING_HELSE_VEST2_XML + " in class path");

        OutboundPostParams params = new OutboundPostParams.Builder()
                .recipientId(participantId.getIdentifier())
                .senderId(participantId.getIdentifier())
                .processId(PeppolProcessIdAcronym.INVOICE_ONLY.stringValue())
                .documentId(PeppolDocumentTypeId.EHF_INVOICE.stringValue()).channelId(new PeppolChannelId("TEST").stringValue()).inputStream(is).build();

        MessageWithLocations message = receiveMessageFromClientUseCase.handleMessage(params);

        //check it's queued
        DatabaseHelper.QueuedMessage queuedMessage = databaseHelper.getQueuedMessageByMsgNo(message.getMsgNo().toLong());
        assertNotNull(queuedMessage);
        assertNotNull(queuedMessage.getQueueId());
        assertEquals(message.getMsgNo().toLong(), queuedMessage.getMsgNo());
        assertEquals(OutboundMessageQueueState.QUEUED, queuedMessage.getState());

        MessageMetaData metaData = peppolMessageRepository.findMessageByMessageNo(message.getMsgNo());
        assertNotNull(metaData);

    }

}
