package no.sr.ringo.peppol;

import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for common methods.
 * 
 * User: andy
 * Date: 1/19/12
 * Time: 9:28 AM
 */
public class RingoUtils {

    static final Logger log = LoggerFactory.getLogger(RingoUtils.class);

    static final DateTimeFormatter iso8601Formatter = ISODateTimeFormat.dateTimeParser();

    public static boolean isEmpty(String value) {
        return value == null ? true : value.trim().length() == 0;
    }

    /**
     * TODO: THIS IS NOT A FEATURE COMPLETE MECHANISM, IF It Doesnt work Create a test and FIX IT!
     * Replaces special characters in regex search strings like .|+() etc...
     *
     * @param value string to quote
     * @return quoted string
     */
    public static String quoteForRegularExpression(String value) {
        if (value == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < value.length()) {
            int point = value.codePointAt(index);
            int count = Character.charCount(point);
            String escaped = quoteCharCode(point);
            if (escaped != null) {
                sb.append(escaped);
            } else {
                sb.appendCodePoint(point);
            }
            index += count;

        }
        return sb.toString();
    }

    /**
     *
     * Encodes all predefined XML entities e.g. & --> &amp;
     * <pre>
     * quot 	" 	U+0022 (34) 	XML 1.0 	double quotation mark
     * amp   	& 	U+0026 (38) 	XML 1.0 	ampersand
     * apos 	' 	U+0027 (39) 	XML 1.0 	apostrophe (= apostrophe-quote)
     * lt 	    &lt; 	U+003C (60) 	XML 1.0 	less-than sign
     * gt 	    &gt;
     * </pre>
     * @param value string to encode
     * @return the encoded string
     */
    public static String encodePredefinedXmlEntities(final String value) {
        if (value == null) {
            return null;
        }
        String toDecode = value.trim();
        //ALWAYS START WITH & So we dont replace the entites we add.
        toDecode = toDecode.replaceAll("&", "&amp;");
        toDecode = toDecode.replaceAll("\"", "&quot;");
        toDecode = toDecode.replaceAll("'", "&apos;");
        toDecode = toDecode.replaceAll("<", "&lt;");
        toDecode = toDecode.replaceAll(">", "&gt;");

        return toDecode;
    }

    /**
     *
     * Decodes all predefined XML entities e.g. &amp;amp; --> &amp;
     *
     * <pre>
     * quot 	" 	U+0022 (34) 	XML 1.0 	double quotation mark
     * amp   	&amp; 	U+0026 (38) 	XML 1.0 	ampersand
     * apos 	' 	U+0027 (39) 	XML 1.0 	apostrophe (= apostrophe-quote)
     * lt 	    &lt; 	U+003C (60) 	XML 1.0 	less-than sign
     * gt 	    &gt;
     * </pre>
     * @param value the string to decode
     * @return the decoded string
     */
    public static String decodePredefinedXmlEntities(final String value) {
        if (value == null) {
            return null;
        }
        String toDecode = value.trim();
        toDecode = toDecode.replaceAll("&quot;", "\"");
        toDecode = toDecode.replaceAll("&apos;", "'");
        toDecode = toDecode.replaceAll("&lt;", "<");
        toDecode = toDecode.replaceAll("&gt;", ">");
        //ALWAYS END WITH & So we dont replace the entites before decoding.
        toDecode = toDecode.replaceAll("&amp;", "&");

        return toDecode;
    }


    private static String quoteCharCode(int code) {
        switch (code) {
            case '/':
                return "\\/";
            default:
                return null;
        }
    }

    /**
     * Encodes the given url so that it is valid in an XML document
     *
     * @param url to format
     * @return the xml representation
     */
    public static String toXml(URI url) {
        return url == null ? "" : encodePredefinedXmlEntities(url.toASCIIString());
    }

    /**
     * Encodes the participant id so it can be used in an XML Document
     *
     * @param sender the ISO6523 sender id
     * @return xml representation
     */
    public static String toXml(ParticipantIdentifier sender) {
        return sender == null ? "" : sender.getIdentifier();
    }

    /**
     * Encodes the channelId so that it can be used in an XML Document
     *
     * @param peppolChannelId peppol channel id
     * @return the xml representation
     */
    public static String toXml(PeppolChannelId peppolChannelId) {
        return peppolChannelId != null ? encodePredefinedXmlEntities(peppolChannelId.stringValue()) : null;
    }

    /**
     * @param peppolDocumentTypeId   document type id
     * @return the xml representation
     */
    public static String toXml(DocumentTypeIdentifier peppolDocumentTypeId) {
        return peppolDocumentTypeId != null ? encodePredefinedXmlEntities(peppolDocumentTypeId.getIdentifier()) : null;
    }

    /**
     * Encodes the peppol document id so that it can be used in an XML Document
     *
     * @param peppolDocumentIdAcronym document acronym
     * @return the xml representation
     * @deprecated
     */
    public static String toXml(PeppolDocumentIdAcronym peppolDocumentIdAcronym) {
        return peppolDocumentIdAcronym != null ? encodePredefinedXmlEntities(peppolDocumentIdAcronym.name()) : null;
    }

    /**
     * @param profileId the process identifier
     * @return XML string representation
     */
    public static String toXml(ProcessIdentifier profileId) {
        return profileId != null ? encodePredefinedXmlEntities(profileId.getIdentifier()) : null;
    }

    /**
     * Encodes the peppol process id so that it can be used in an XML Document
     *
     * @param peppolProcessId the peppol process id
     * @return String representation
     * @deprecated
     */
    public static String toXml(PeppolProcessIdAcronym peppolProcessId) {
        return peppolProcessId != null ? encodePredefinedXmlEntities(peppolProcessId.name()) : null;
    }

    public static String toXml(String string) {
        if (string == null) {
            return "";
        }
        return encodePredefinedXmlEntities(string);
    }

    /**
     * Parses the date time from an IOS8601 formatted string.
     *
     * @param dateString the date string to parse
     * @return parsed date
     */
    public static Date getDateTimeFromISO8601String(String dateString) {

        if (dateString == null || dateString.trim().length() == 0) {
            return null;
        }
        DateTime dateTime = iso8601Formatter.parseDateTime(dateString);
        return dateTime.toDate();
    }

    /**
     * Formats the date time as an ISO8601 formatted string
     *
     * @param date the date to format
     * @return iso8601 formatted string
     */
    public static String formatDateTimeAsISO8601String(Date date) {
        if (date == null) {
            return "";
        }
        return ISODateTimeFormat.dateTime().print(date.getTime());
    }

    /**
     * Gets a resource from a JAR file as a StringBuffer.
     *
     * @param cls          The class to get the class loader from. If null, this class is used
     * @param resourceName The name of the resource, e.g "/no/sendregning/resources/eula.txt".
     * @return The resource as a StringBuffer.
     */
    public static StringBuffer getResourceFromJar(Class cls, final String resourceName) {

        /**
         * validating the parameters before use
         */
        if (cls == null) {
            cls = RingoUtils.class;
        }

        if (resourceName == null || resourceName.trim().length() == 0) {
            throw new IllegalArgumentException("Parameter resourceName can't be null or zero length");
        }

        try {

            InputStream resourceAsStream = cls.getResourceAsStream(resourceName);
            if (resourceAsStream == null) {
                throw new RuntimeException("Unable to find resouce : " + resourceName);
            }
            final BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
            final StringBuffer result = new StringBuffer();
            String tmp;

            while ((tmp = br.readLine()) != null) {
                result.append(tmp).append("\n");
            }

            return result;
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Sets time (hour, min, sec) of given date to specified values
     *
     * @param date date
     * @param hour hour
     * @param min  minute
     * @param sec  second
     * @return completed date time
     */
    public static Date setTimeOnDate(Date date, int hour, int min, int sec) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR, hour);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, sec);

        return c.getTime();
    }

}
