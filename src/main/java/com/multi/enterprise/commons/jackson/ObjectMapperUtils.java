package com.multi.enterprise.commons.jackson;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

public abstract class ObjectMapperUtils {
    private static final Logger log = LoggerFactory.getLogger( ObjectMapperUtils.class );

    public static TypeResolverBuilder<?> createClassTypeResolver() {
        return createTypeResolver( DefaultTyping.NON_FINAL, JsonTypeInfo.Id.CLASS, JsonTypeInfo.As.PROPERTY, "_class" );
    }

    public static TypeResolverBuilder<?> createNameTypeResolver() {
        return createTypeResolver( DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.Id.NAME, JsonTypeInfo.As.PROPERTY, "_type" );
    }

    public static TypeResolverBuilder<?> createTypeResolver( final DefaultTyping applicability, final JsonTypeInfo.Id idType, final JsonTypeInfo.As asType, final String propertyName ) {
        return new DefaultTypeResolverBuilder( applicability ).init( idType, null ).inclusion( asType ).typeProperty( propertyName );
    }

    public static ObjectMapper createWithNameTypeResolver( final ObjectMapperMixinInitializer mixinInitializer ) {
        return createWithTypeResolver( createNameTypeResolver(), mixinInitializer );
    }

    public static ObjectMapper createWithClassTypeResolver( final ObjectMapperMixinInitializer mixinInitializer ) {
        return createWithTypeResolver( createClassTypeResolver(), mixinInitializer );
    }

    public static ObjectMapper createWithTypeResolver( final TypeResolverBuilder<?> builder, final ObjectMapperMixinInitializer mixinInitializer ) {
        Class<?>[] mixins = null;
        if ( mixinInitializer != null ) {
            mixins = mixinInitializer.getMixins();
        }
        return ArrayUtils.isEmpty( mixins ) ? createWithTypeResolver( builder ) : createWithTypeResolver( builder, mixins );
    }

    public static ObjectMapper createWithClassTypeResolver( final Class<?>... mixinPairs ) {
        return createWithTypeResolver( createClassTypeResolver(), mixinPairs );
    }

    public static ObjectMapper createWithTypeResolver( final TypeResolverBuilder<?> builder, final Class<?>... mixinPairs ) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker( getAnyFieldOnlyVisibility( mapper ) );
        if ( builder != null ) {
            mapper.setDefaultTyping( builder );
        }
        mapper.configure( SerializationFeature.FAIL_ON_EMPTY_BEANS, false );
        if ( !ArrayUtils.isEmpty( mixinPairs ) ) {
            if ( mixinPairs.length % 2 != 0 ) {
                throw new IllegalArgumentException( "mixinPairs must be pairs of target class and mixin class" );
            }
            for ( int i = 0; i < mixinPairs.length / 2; i++ ) {
                log.debug( "adding jackson mixin: {} -> {}", mixinPairs[i].getName(), mixinPairs[i + 1].getName() );
                mapper.addMixInAnnotations( mixinPairs[i * 2], mixinPairs[i * 2 + 1] );
            }
        }

        return mapper;
    }

    public static ObjectMapper createWithDefaults( final ObjectMapperMixinInitializer mixinInitializer ) {
        return createWithDefaults( true, mixinInitializer );
    }

    public static ObjectMapper createWithDefaults( final boolean defaultTypingEnabled, final ObjectMapperMixinInitializer mixinInitializer ) {
        return createWithTypeResolver( defaultTypingEnabled ? createClassTypeResolver() : null, mixinInitializer );
    }

    public static ObjectMapper createWithDefaults( final Class<?>... mixinPairs ) {
        return createWithClassTypeResolver( mixinPairs );
    }

    public static VisibilityChecker<?> getAnyFieldOnlyVisibility( final ObjectMapper mapper ) {
        final VisibilityChecker<?> checker = mapper.getSerializationConfig().getDefaultVisibilityChecker();
        return checker.withFieldVisibility( Visibility.ANY ).withGetterVisibility( Visibility.NONE ).withSetterVisibility( Visibility.NONE ).withCreatorVisibility( Visibility.NONE ).withIsGetterVisibility( Visibility.NONE );
    }
}
