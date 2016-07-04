package com.multi.enterprise.commons.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;

import org.apache.commons.lang3.ObjectUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InterfaceWithGenerics JaxbContext resolver that preloads and manages JaxbContexts for a given base package.
 * Additionally, once a Jaxb2ContextResolver instance has been instantiated, it can be looked up via static methods for ease of access.
 * See JAXBUtils as example.
 *
 *
 */
public class Jaxb2ContextResolver {
    private static final Logger log = LoggerFactory.getLogger( Jaxb2ContextResolver.class );
    private static final ConcurrentHashMap<String, Jaxb2ContextResolver> resolvers = new ConcurrentHashMap<String, Jaxb2ContextResolver>();

    private final Map<String, JAXBContext> jaxbContexts = new ConcurrentHashMap<String, JAXBContext>();

    public Jaxb2ContextResolver( final String basePackage, final String... dependentPackages ) {
        this.initializeContext( basePackage, dependentPackages );
        resolvers.put( basePackage, this );
    }

    public static Jaxb2ContextResolver getInstance( final Class<?> clazz ) {
        return getInstance( clazz.getPackage() );
    }

    public static Jaxb2ContextResolver getInstance( final Package pkg ) {
        return getInstance( pkg.getName() );
    }

    public static boolean exists( final String pkgName ) {
        return resolvers.containsKey( pkgName );
    }

    private static Jaxb2ContextResolver getInstance( final String pkgName ) {
        final Jaxb2ContextResolver resolver = resolvers.get( pkgName );
        if ( resolver != null ) {
            return resolver;
        }
        if ( org.apache.commons.lang3.StringUtils.countMatches( pkgName, "." ) <= 1 ) {
            return null;
        }
        return getInstance( pkgName.substring( 0, pkgName.lastIndexOf( "." ) ) );
    }

    /** will return null if specified class isn't in the base package and its sub-packages. */
    public JAXBContext getContext( final Class<?> clazz ) throws JAXBException {
        return this.jaxbContexts.get( clazz.getName() );
    }

    private <T extends Annotation> void addAllClassesWithAnnotation( final Class<T> annotationClass, final String basePackage, final Collection<Class<?>> allClasses ) {
        final Reflections reflections = new Reflections( basePackage );
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith( annotationClass );
        for ( final Class<?> clz : classes ) {
            if ( !allClasses.contains( clz ) ) {
                allClasses.add( clz );
            }
        }
    }

    private <T extends Annotation> List<Class<?>> getAllClassesWithAnnotation( final Class<T> annotationClass, final String basePackage, final String... dependentPackages ) {
        final List<Class<?>> allClasses = new ArrayList<Class<?>>();
        for ( final String dependentPackage : ObjectUtils.defaultIfNull( dependentPackages, new String[0] ) ) {
            this.addAllClassesWithAnnotation( annotationClass, dependentPackage, allClasses );
        }
        this.addAllClassesWithAnnotation( annotationClass, basePackage, allClasses );
        return allClasses;
    }

    private void initializeContext( final String basePackage, final String... dependentPackages ) {
        try {
            log.info( "Initializing with base package {} and dependent packages {}", basePackage, Arrays.toString( dependentPackages ) );

            final List<Class<?>> classes = this.getAllClassesWithAnnotation( XmlRootElement.class, basePackage, dependentPackages );

            final Class<?>[] classesArray = classes.toArray( new Class<?>[0] );
            final JAXBContext jaxbContext = JAXBContext.newInstance( classesArray );
            for ( final Class<?> c : classes ) {
                this.jaxbContexts.put( c.getName(), jaxbContext );
            }
            this.jaxbContexts.put( basePackage, jaxbContext );

            final Map<String, List<Class<?>>> classesByNsMap = new HashMap<String, List<Class<?>>>();
            final Map<String, List<Package>> packagesByNsMap = new HashMap<String, List<Package>>();
            this.populateNsMaps( classes, classesByNsMap, packagesByNsMap, basePackage );
            for ( final String ns : classesByNsMap.keySet() ) {
                final List<Class<?>> subset = classesByNsMap.get( ns );
                if ( subset == null || subset.size() <= 0 ) {
                    continue;
                }
                final Class<?>[] subsetArray = subset.toArray( new Class<?>[0] );
                final JAXBContext jContext = JAXBContext.newInstance( subsetArray );
                for ( final Class<?> c : subsetArray ) {
                    this.jaxbContexts.put( c.getName(), jContext );
                }

                final List<Package> pkgs = packagesByNsMap.get( ns );
                if ( pkgs == null || pkgs.size() <= 0 ) {
                    continue;
                }
                for ( final Package p : pkgs ) {
                    this.jaxbContexts.put( p.getName(), jContext );
                }
            }

        } catch ( final JAXBException e ) {
            log.error( "Could not initialized default context", e );
        }

    }

    private void populateNsMaps( final Collection<Class<?>> classes, final Map<String, List<Class<?>>> classesByNsMap, final Map<String, List<Package>> packagesByNsMap, final String basePackage ) {
        for ( final Class<?> clz : classes ) {
            final Package pkg = clz.getPackage();
            if ( pkg.getName().equalsIgnoreCase( basePackage ) ) {
                continue;
            }
            final String ns = this.getPackageNs( pkg );
            if ( ns != null && ns.length() > 0 ) {
                addToMap( classesByNsMap, ns, clz );
                addToMap( packagesByNsMap, ns, pkg );
            }
        }
    }

    private static <T> void addToMap( final Map<String, List<T>> map, final String key, final T value ) {
        List<T> list = map.get( key );
        if ( list == null ) {
            list = new ArrayList<T>();
            map.put( key, list );
        }
        list.add( value );
    }

    private String getPackageNs( final Package pkg ) {
        final XmlSchema schema = pkg.getAnnotation( XmlSchema.class );
        if ( schema == null ) {
            return null;
        }
        final String ns = schema.namespace();
        if ( ns == null || ns.length() <= 0 ) {
            return null;
        }
        return ns;
    }
}
