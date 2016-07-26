package com.multi.enterprise.types.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

public class XmlCDataUtils {
    private static final Logger log = Logger.getLogger( XmlCDataUtils.class );

    private static Map<String, String> xmlCDataNamesMap = Collections.synchronizedMap( new HashMap<String, String>() );

    private static String initXmlCDataNames( String basePackage ) {
        log.info( "Initializing with base package: " + basePackage );
        Reflections reflections = new Reflections( basePackage );
        Set<Class<?>> xmlClasses = reflections.getTypesAnnotatedWith( XmlRootElement.class );
        Set<Class<?>> xmlCDataClasses = reflections.getTypesAnnotatedWith( XmlCData.class );
        xmlCDataClasses.retainAll( xmlClasses );

        Set<String> xmlCDataNameSet = new HashSet<String>();
        for ( Class<?> clz : xmlCDataClasses ) {
            XmlCData xmlCData = clz.getAnnotation( XmlCData.class );
            if ( xmlCData != null && xmlCData.names().trim().length() > 0 ) {
                String[] list = xmlCData.names().split( " " );
                for ( String part : list ) {
                    xmlCDataNameSet.add( part );
                }
            }
        }
        String names = StringUtils.join( xmlCDataNameSet.iterator(), ' ' );
        xmlCDataNamesMap.put( basePackage, names );

        return names;
    }

    public static String getXmlCDataNames( String basePackage ) {
        synchronized ( xmlCDataNamesMap ) {
            String names = xmlCDataNamesMap.get( basePackage );
            if ( names != null ) {
                return names;
            }
            return initXmlCDataNames( basePackage );
        }
    }

    public static String getXmlCDataNames( Object object ) {
        XmlCData annotation = object.getClass().getAnnotation( XmlCData.class );
        return annotation == null ? null : annotation.names();
    }

    public static void transform( String xml, String charSet, String cdataElements, Result result ) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        if ( cdataElements != null ) {
            transformer.setOutputProperty( OutputKeys.CDATA_SECTION_ELEMENTS, cdataElements );
        }
        if ( charSet != null ) {
            transformer.setOutputProperty( OutputKeys.ENCODING, charSet );
        }
        transformer.transform( new StreamSource( new StringReader( xml ) ), result );
    }

    public static String transform( String xml, String cdataElements ) throws TransformerException {
        if ( cdataElements == null ) {
            return xml;
        }

        StringWriter writer = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.CDATA_SECTION_ELEMENTS, cdataElements );
        transformer.transform( new StreamSource( new StringReader( xml ) ), new StreamResult( writer ) );
        return writer.toString();
    }

}