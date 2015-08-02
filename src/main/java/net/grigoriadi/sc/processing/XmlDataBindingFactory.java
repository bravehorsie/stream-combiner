package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppContext;
import net.grigoriadi.sc.processing.generation.IStreamGenerator;
import net.grigoriadi.sc.processing.generation.JaxbStreamGenerator;
import net.grigoriadi.sc.processing.generation.StaxStreamGenerator;
import net.grigoriadi.sc.processing.generation.SumAmountsListener;
import net.grigoriadi.sc.processing.parsing.*;

/**
 * Factory for creation stream generators / parsers.
 */
public class XmlDataBindingFactory {

    private static final String XML_DATA_GENERATOR_TYPE = "XML_DATA_GENERATOR_TYPE";

    private static final String XML_DATA_PARSER_TYPE = "XML_DATA_PARSER_TYPE";

    public synchronized static IStreamGenerator newStreamGenerator() {
        String xmlDataGeneratorProperty = (String) AppContext.getInstance().getProperties().get(XML_DATA_GENERATOR_TYPE);
        checkProperty(xmlDataGeneratorProperty);
        switch (SupportedXmlBindingTypes.valueOf(xmlDataGeneratorProperty)) {
            case JAXB:
                return new JaxbStreamGenerator(new SumAmountsListener());
            case STAX:
                return new StaxStreamGenerator(new SumAmountsListener());
            case SAX:
                default:
                throw new IllegalArgumentException("SAX is not supported for XML data generation");
        }
    }

    public synchronized static IStreamParser newStreamParser(String clientId) {
        String xmlDataGeneratorProperty = (String) AppContext.getInstance().getProperties().get(XML_DATA_PARSER_TYPE);
        checkProperty(xmlDataGeneratorProperty);
        switch (SupportedXmlBindingTypes.valueOf(xmlDataGeneratorProperty)) {
            case JAXB:
                return new JAXBParser(new ItemHandler(clientId));
            case STAX:
                return new StaxParser(new ItemHandler(clientId));
            case SAX:
                return new SaxParser(new ItemHandler(clientId));
            default:
                throw new IllegalArgumentException("Parser type is not supported.");
        }
    }



    private static void checkProperty(String property) {
        for (SupportedXmlBindingTypes value : SupportedXmlBindingTypes.values()) {
            if (value.name().equals(property)) {
                return;
            }
        }
        throw new IllegalArgumentException(property + " is not supported. Use JAXB, STAX, or SAX.");
    }
}
