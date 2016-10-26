package no.sr.ringo.document;

import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;
import no.sr.ringo.account.RingoAccount;
import no.sr.ringo.guice.jdbc.JdbcTxManager;
import no.sr.ringo.guice.jdbc.Repository;
import no.sr.ringo.message.MessageNumber;
import no.sr.ringo.message.PeppolMessageNotFoundException;
import no.sr.ringo.peppol.PeppolDocumentTypeId;
import no.sr.ringo.utils.SbdhUtils;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {

    private final PeppolDocumentFactory documentFactory;
    private final JdbcTxManager jdbcTxManager;

    @Inject
    public DocumentRepositoryImpl(PeppolDocumentFactory documentFactory,JdbcTxManager jdbcTxManager) {
        this.documentFactory = documentFactory;
        this.jdbcTxManager = jdbcTxManager;
    }

    @Override
    public PeppolDocument getPeppolDocument(RingoAccount ringoAccount, MessageNumber msgNo) {
        try {

            return fetchPeppolDocument(ringoAccount, msgNo);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to retrieve xml document for message no: " + msgNo, e);
        }
    }

    //
    // utility functions should be kept private to avoid being intercepted by the Repository annotation
    //

    private PeppolDocument fetchPeppolDocument(RingoAccount ringoAccount, MessageNumber msgNo) throws SQLException {
        ResultSet rs = fetchResultSet(ringoAccount, msgNo);
        if (documentFound(rs)) {
            return extractPeppolDocumentFromResultSet(rs);
        } else {
            throw new PeppolMessageNotFoundException(msgNo.toLong());
        }
    }

    private boolean documentFound(ResultSet rs) throws SQLException {
        return rs.next();
    }

    private ResultSet fetchResultSet(RingoAccount ringoAccount, MessageNumber msgNo) throws SQLException {
        final Connection con = jdbcTxManager.getConnection();
        PreparedStatement ps = prepareSelect(ringoAccount, msgNo, con);
        return ps.executeQuery();
    }

    private PeppolDocument extractPeppolDocumentFromResultSet(ResultSet rs) throws SQLException {
        String documentId = rs.getString("document_id");  // Document type id

        String payloadUrl = rs.getString("payload_url");


        // Loads all the lines from the file and joins them with NL
        String xmlMessage = null;
        try (
            Stream<String> stringStream = Files.lines(Paths.get(URI.create(payloadUrl)), Charset.forName("UTF-8"))){
            xmlMessage = stringStream.collect(joining(System.lineSeparator()));
            // Removes the SBDH if there is one.
            xmlMessage = SbdhUtils.removeSbdhEnvelope(xmlMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
        String xmlMessage;
        try (SbdReader sbdReader = SbdReader.newInstance(Files.newInputStream(Paths.get(URI.create(payloadUrl)))) ) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamUtils.copy(sbdReader.xmlReader(), baos);
            xmlMessage = new String(baos.toString("UTF-8"));
        } catch (SbdhException | IOException | XMLStreamException e) {
            throw new IllegalStateException("Unable to read payload from " + payloadUrl + "\n" + e.getMessage(),e);
        }
*/

        return documentFactory.makePeppolDocument(PeppolDocumentTypeId.valueFor(documentId), xmlMessage);
    }

    private PreparedStatement prepareSelect(RingoAccount ringoAccount, MessageNumber msgNo, Connection con) throws SQLException {
        // dumpDbmsMetaData(con);

        PreparedStatement ps = con.prepareStatement("select document_id, payload_url from message where msg_no=? and account_id = ?");
        ps.setInt(1, msgNo.toInt());
        ps.setInt(2, ringoAccount.getId().toInteger());
        return ps;
    }

    private void dumpDbmsMetaData(Connection con) throws SQLException {
        ResultSet rs = con.getMetaData().getTables(null, null, null, null);
        while (rs.next()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println(metaData.getColumnLabel(i) + " :" + rs.getString(i));
            }
            System.out.println();
        }
    }

}
