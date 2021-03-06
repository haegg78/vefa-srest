/* Created by steinar on 08.01.12 at 22:20 */
package no.sr.ringo.response;

import no.sr.ringo.message.MessageWithLocations;
import no.sr.ringo.peppol.PeppolHeader;
import no.sr.ringo.peppol.RingoUtils;

/**
 * Class representing the result of /messages/{msg_no} request
 * @author adam
 */
public class SingleMessagesResponse implements RestResponse {

    protected static final String version = "1.0";

    protected final MessageWithLocations message;

    public SingleMessagesResponse(MessageWithLocations messageWithLocator) {
        this.message = messageWithLocator;
    }

    public String asXml() {
        StringBuilder resultXml = new StringBuilder();
        resultXml.append("<messages-query-response version=\""+version+"\">\n");
        SingleMessagesResponse.singleMessageAsXml(resultXml, message);
        resultXml.append("</messages-query-response>");
        return resultXml.toString();
    }

    public String getVersion() {
        return version;
    }

    public MessageWithLocations getMessage() {
        return message;
    }

    protected static void singleMessageAsXml(StringBuilder xml, MessageWithLocations current) {
        xml.append("<message>\n");
        xml.append("        <self>" + RingoUtils.toXml(current.getSelfURI()) + "</self>\n");
        xml.append("        <xml-document>" + RingoUtils.toXml(current.getXmlDocumentURI()) + "</xml-document>\n");
        xml.append("        <message-meta-data>\n");
        xml.append("          <msg-no>" + current.getMsgNo() + "</msg-no>\n");
        xml.append("          <direction>" + current.getTransferDirection().name() + "</direction>\n");
        xml.append("          <received>" + RingoUtils.formatDateTimeAsISO8601String(current.getReceived()) + "</received>\n");
        if (current.getDelivered() != null){
            xml.append("          <delivered>" + RingoUtils.formatDateTimeAsISO8601String(current.getDelivered()) + "</delivered>\n");
        }
        if (current.getReceptionId() != null) {
            xml.append("          <uuid>" + RingoUtils.encodePredefinedXmlEntities(current.getReceptionId().stringValue()) + "</uuid>\n");
        }
        peppolHeaderAsXml(xml, current.getPeppolHeader());
        xml.append("        </message-meta-data>\n");
        xml.append("</message>\n");
    }

    private static void peppolHeaderAsXml(StringBuilder xml, PeppolHeader peppolHeader) {
        xml.append("          <peppol-header>\n");
        xml.append("            <sender>" + RingoUtils.toXml(peppolHeader.getSender()) + "</sender>\n");
        xml.append("            <receiver>" + RingoUtils.toXml(peppolHeader.getReceiver()) + "</receiver>\n");
        xml.append("            <channel>" + RingoUtils.toXml(peppolHeader.getPeppolChannelId()) + "</channel>\n");
        // TODO: Consider replacing this with something else
        // xml.append("            <document-type>" + RingoUtils.toXml(peppolHeader.getPeppolDocumentIdAcronym()) + "</document-type>\n");
        xml.append("            <document-id>" + RingoUtils.toXml(peppolHeader.getPeppolDocumentTypeId()) + "</document-id>\n");
        // TODO: Consider replacing this with something else
        // xml.append("            <process-name>" + RingoUtils.toXml(peppolHeader.getPeppolProcessIdAcronym()) + "</process-name>\n");
        xml.append("            <process-id>" + RingoUtils.toXml(peppolHeader.getProcessIdentifier()) + "</process-id>\n");
        xml.append("          </peppol-header>\n");
    }
}
