package com.multi.enterprise.commons.jaxb;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class Jaxb2MessageConverter extends Jaxb2RootElementHttpMessageConverter {
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    @Override
    protected Object readFromSource( final Class<?> clazz, final HttpHeaders headers, final Source source ) throws IOException {
        try {
            final JAXBContext context = this.getSmartContext( clazz );
            final Unmarshaller unmarshaller = context.createUnmarshaller();

            final JAXBElement<?> jaxbElement = unmarshaller.unmarshal( source, clazz );
            return jaxbElement.getValue();
        } catch ( final UnmarshalException ex ) {
            throw new HttpMessageNotReadableException( "Could not unmarshal to [" + clazz + "]: " + ex.getMessage(), ex );

        } catch ( final JAXBException ex ) {
            throw new HttpMessageConversionException( "Could not instantiate JAXBContext: " + ex.getMessage(), ex );
        }
    }

    @Override
    protected void writeToResult( final Object o, final HttpHeaders headers, final Result result ) throws IOException {
        try {
            final Class<?> clazz = ClassUtils.getUserClass( o );
            final JAXBContext context = this.getSmartContext( clazz );
            final Marshaller marshaller = context.createMarshaller();
            this.setCharset( headers.getContentType(), marshaller );
            marshaller.marshal( o, result );
        } catch ( final MarshalException ex ) {
            throw new HttpMessageNotWritableException( "Could not marshal [" + o + "]: " + ex.getMessage(), ex );
        } catch ( final JAXBException ex ) {
            throw new HttpMessageConversionException( "Could not instantiate JAXBContext: " + ex.getMessage(), ex );
        }
    }

    private JAXBContext getSmartContext( final Class<?> clazz ) throws JAXBException {
        Assert.notNull( clazz, "'clazz' must not be null" );
        return JAXBUtils.getContext( clazz );
    }

    private void setCharset( final MediaType contentType, final Marshaller marshaller ) throws PropertyException {
        if ( contentType != null && contentType.getCharSet() != null ) {
            marshaller.setProperty( Marshaller.JAXB_ENCODING, contentType.getCharSet().name() );
        }
    }
}