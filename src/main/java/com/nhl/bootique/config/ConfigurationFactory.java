package com.nhl.bootique.config;

import com.nhl.bootique.type.TypeRef;

/**
 * An object that provides access to a tree of configuration data. The whole
 * tree or subtrees of configuration can be read by the factory users as objects
 * of a specified type. More often than not returned configuration objects are
 * themselves "factories" of various services. So ConfigurationFactory can be
 * thought as a "factory of factories".
 * 
 * @since 0.10
 */
public interface ConfigurationFactory {

	/**
	 * Creates and returns a specified type instance with its state initialized
	 * from the configuration tree. "prefix" argument defines sub-configuration
	 * location in the tree.
	 * 
	 * @param type
	 *            a type of configuration object to create.
	 * @param prefix
	 *            sub-configuration location in the config tree. Pass empty
	 *            string to access root config.
	 * @return a fully initialized object of the specified type.
	 * 
	 * @param <T>
	 *            a type of object given configuration should be deserialized
	 *            to.
	 */
	<T> T config(Class<T> type, String prefix);

	/**
	 * Creates and returns a specified generic type instance with its state
	 * initialized from the configuration tree. "prefix" argument defines
	 * sub-configuration location in the tree. To make a proper "type"
	 * parameter, you would usually create an anonymous inner subclass of
	 * TypeRef, with the right generics parameters:
	 * 
	 * <pre>
	 * new TypeRef&lt;List&lt;Object&gt;&gt;() {
	 * }
	 * </pre>
	 * 
	 * @param type
	 *            a type of parameterized factory to create. You must create a
	 *            subclass of {@link TypeRef} with correct generics parameters.
	 * @param prefix
	 *            sub-configuration location in the config tree. Pass empty
	 *            string to access root config.
	 * @return a fully initialized object of the specified type.
	 * 
	 * @param <T>
	 *            a type of object given configuration should be deserialized
	 *            to.
	 * @since 0.9
	 */
	<T> T config(TypeRef<? extends T> type, String prefix);

}
