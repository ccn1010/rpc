package com.my.server.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public class ImplementationContainer{

    private final Map<Class< ? >, Object> implByClass = new LinkedHashMap<Class< ? >, Object>();

    public <T> void addImplementation( Class<T> clazz, T impl ){
    	if( clazz == null ) throw new NullPointerException( "interfaceClazz is null" );
        if( impl == null ) throw new NullPointerException( "implementation is null" );
        if( !clazz.isInterface() ) throw new IllegalArgumentException( "no interface" );
        if( this.containsImplementation( clazz ) ) throw new IllegalArgumentException( "duplicate implementation" );
        implByClass.put( clazz, impl );
    }

    public boolean containsImplementation( Class< ? > clazz ){
        return implByClass.containsKey( clazz );
    }

    public boolean containsImplementation( String fqClassName ){
        for( Class< ? > clazz : implByClass.keySet() ){
            if( clazz.getName().equals( fqClassName ) ) return true;
        }
        return false;
    }

    public Object getImplementation( Class< ? > clazz ){
        return implByClass.get( clazz );
    }

    public Object getImplementation( String fqClassName ){
        for( Class< ? > clazz : implByClass.keySet() ){
            if( clazz.getName().equals( fqClassName ) ) return implByClass.get( clazz );
        }
        return null;
    }
    
    public void removeImplementation(Class< ? > clazz){
    	implByClass.remove(clazz);
    }
    
    public void removeImplementation(String fqClassName){
    	for( Class< ? > clazz : implByClass.keySet() ){
            if( clazz.getName().equals( fqClassName ) ){
            	implByClass.remove(clazz);
            	return;
            }
        }
    }
    
    

}
