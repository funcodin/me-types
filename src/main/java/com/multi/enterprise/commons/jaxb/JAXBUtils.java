package com.multi.enterprise.commons.jaxb;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.multi.enterprise.commons.utils.ClassNameUtils;
import com.multi.enterprise.commons.utils.Jaxb2ContextResolver;
import com.multi.enterprise.commons.utils.XmlCDataUtils;

/**
 * This creates and caches JAXBContext instances. JAXBContext instances are created based on the objects to be marshalled or unmarshalled.
 * Any abstract/genric fields are expected to have references to concrete type classes, using either {@link javax.xml.bind.annotation.XmlSeeAlso} or
 * {@link javax.xml.bind.annotation.XmlElementRefs}.
 * <p>
 * Update: an improvement has been made to leverage Jaxb2ContextResolver's ability to preload all classes from a base package to get around
 * generics/interfaces/abstract. To take advantage of this improvement, one should first instantiate a Jaxb2ContextResolver with the desired base package that
 * contains all classes that JAXBUtils will be marshalling/unmarshalling.
 * <p>
 * In addition to caching of JAXBContext instances, when unmarshalling, it also validates the input Xml against the expected type with
 * {@link javax.xml.bind.annotation.XmlRootElement} annotation. If {@link javax.xml.bind.annotation.XmlRootElement#name} is defined but doesn't match root
 * element of the input Xml, the input Xml will be rejected and UnmarshalException will be thrown.
 *
 * @author xu_a
 */
public abstract class JAXBUtils {
    private static final Map<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<Class<?>, JAXBContext>();

    public static JAXBContext getContext( final Class<?> clz ) throws JAXBException {
        JAXBContext context = contexts.get( clz );

        if ( context != null ) {
            return context;
        }
        final Jaxb2ContextResolver resolver = Jaxb2ContextResolver.getInstance( clz );
        if ( resolver != null ) {
            context = resolver.getContext( clz );
        }
        if ( context == null ) {
            context = JAXBContext.newInstance( clz );
        }
        contexts.put( clz, context );

        return context;
    }

    public static <T> T unmarshal( final InputStream is, final Class<T> clz ) throws Exception {
        return unmarshal( new InputStreamReader( is ), clz );
    }

    public static <T> T unmarshal( final Reader reader, final Class<T> clz ) throws Exception {
        final JAXBContext context = getContext( clz );
        final Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
        final XMLInputFactory xif = XMLInputFactory.newInstance();
        final XMLEventReader xer = xif.createXMLEventReader( reader );
        final String rootElementName = getXmlRootElementName( clz );
        if ( rootElementName != null && rootElementName.length() > 0 ) {
            validateXmlRootElement( xer, rootElementName );
        }
        final JAXBElement<T> element = jaxbUnmarshaller.unmarshal( xer, clz );
        return element.getValue();
    }

    private static StartElement peekRootElement( final XMLEventReader xer ) throws XMLStreamException {
        XMLEvent event = xer.peek();
        while ( event != null && !event.isStartElement() ) {
            xer.next();
            event = xer.peek();
        }
        if ( event == null ) {
            return null;
        }
        return event.asStartElement();
    }

    private static void validateXmlRootElement( final XMLEventReader xer, final String rootElementName ) throws Exception {
        final StartElement element = peekRootElement( xer );
        if ( element == null ) {
            throw new UnmarshalException( "xml doesn't contain any element." );
        }
        final String xmlElementName = element.getName().getLocalPart();
        if ( !xmlElementName.equals( rootElementName ) ) {
            throw new UnmarshalException( "xml does not match declaredType "+ rootElementName ) ;
        }
    }

    public static void marshal( final Object object, final OutputStream os, final boolean formatted ) throws Exception {
        marshal( object, new OutputStreamWriter( os ), formatted );
    }

    public static void marshal( final Object object, final Writer writer, final boolean formatted ) throws Exception {
        final JAXBContext context = getContext( object.getClass() );
        final Marshaller jaxbMarshaller = context.createMarshaller();
        jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, formatted );
        jaxbMarshaller.marshal( object, writer );
        writer.flush();
    }

    public static String toXmlString( final Object object, final boolean formatted ) throws Exception {
        return toXmlString( object, formatted, null );
    }

    public static String toXmlString( final Object object, final boolean formatted, String cdataElements ) throws Exception {
        // 1. marshal object into Xml
        final StringWriter marshalingWriter = new StringWriter();
        marshal( object, marshalingWriter, formatted );
        final String xml = marshalingWriter.toString();

        // 2. check if cdata is required, either from cdataElements param, or XmlCData annotation with object param
        cdataElements = resolveCDataElements( cdataElements, object );
        if ( cdataElements == null ) {
            return xml;
        }

        // 3. transform to honor cdata as necessary
        final StringWriter writer = new StringWriter();
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, formatted ? "yes" : "no" );
        transformer.setOutputProperty( OutputKeys.CDATA_SECTION_ELEMENTS, cdataElements );
        transformer.transform( new StreamSource( new StringReader( xml ) ), new StreamResult( writer ) );
        return writer.toString();
    }

    private static String resolveCDataElements( final String cdataElements, final Object object ) throws Exception {
        final String globalCDataElements = XmlCDataUtils.getXmlCDataNames( object );
        if ( globalCDataElements == null || globalCDataElements.trim().length() <= 0 ) {
            return cdataElements;
        }
        if ( cdataElements == null || cdataElements.trim().length() <= 0 ) {
            return globalCDataElements;
        }
        return cdataElements + " " + globalCDataElements;
    }

    private static String getXmlRootElementName( final Class<?> clz ) {
        final XmlRootElement rootElement = clz.getAnnotation( XmlRootElement.class );
        if ( rootElement == null ) {
            return null;
        }
        final String name = rootElement.name();
        try {
            if ( name.equals( getDefaultValue( XmlRootElement.class, "name" ) ) ) {
                return ClassNameUtils.toVariableName( clz.getSimpleName() );
            }
        } catch ( final Exception ex ) {
        }
        return name;
    }

    @SuppressWarnings("unchecked")
    private static <T, A extends Annotation> T getDefaultValue( final Class<A> annotationClass, final String methodName ) throws SecurityException, NoSuchMethodException {
        return (T) annotationClass.getMethod( methodName ).getDefaultValue();
    }
}