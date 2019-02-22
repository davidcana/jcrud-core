package org.github.davidcana.jcrud.storages;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.github.davidcana.jcrud.core.ZCrudEntity;
import org.github.davidcana.jcrud.core.annotations.JCRUDEntity;
import org.github.davidcana.jcrud.core.options.Option;
import org.github.davidcana.jcrud.core.options.OptionProvider;

abstract public class AbstractStorage<T extends ZCrudEntity, K> implements Storage<T, K> {
	
	protected Class<T> clazz;
	
	protected AbstractStorage(){
		this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	protected AbstractStorage(Class<T> clazz){
		this.clazz = clazz;
	}
	
	@Override
	public List<Option<K>> getAllAsOptions() throws StorageException {
		
		try {
			List<Option<K>> result = new ArrayList<>();
			
			for (T object : this.getAll()){
				OptionProvider<K> optionProvider = (OptionProvider<K>) object;
				result.add(
						optionProvider.buildOption());
			}
			
			return result;
			
		} catch (ClassCastException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public Class<?> getDeserializeClass() {
		return this.clazz;
	}
	
	private void resolveSuperClassAnnotation(Annotation annotation){
		
	    if (annotation instanceof JCRUDEntity){
	    	this.resolveJCRUDEntity( (JCRUDEntity) annotation);
	    	return;
	    }
	    
	    if (! this.resolveClassAnnotation(annotation)){
	    	throw new IllegalArgumentException("Unknown class annotation: " + annotation);
	    }
	}
	
	abstract protected boolean resolveClassAnnotation(Annotation annotation);
	
	private void resolveJCRUDEntity(JCRUDEntity annotation) {
		// Nothing to do
	}
	
	private void resolveSuperFieldAnnotation(Annotation annotation, Field field) throws StorageException{
	    
		if (! this.resolveFieldAnnotation(annotation, field)){
	    	throw new IllegalArgumentException("Unknown field annotation: " + annotation);
		}
	}
	
	abstract protected boolean resolveFieldAnnotation(Annotation annotation, Field field) throws StorageException;
	
	protected void resolveAll() throws StorageException {
		
		// Get annotations of class
		for (Annotation annotation : this.clazz.getAnnotations()){
			this.resolveSuperClassAnnotation(annotation);
		}
		
		// Get annotations of fields
		for (Field field : this.clazz.getDeclaredFields()){
			for (Annotation annotation : field.getAnnotations()){
				this.resolveSuperFieldAnnotation(annotation, field);
			}
		}
	}
	
}
