package net.collaud.fablab.gcodesender.tools;

/**
 *
 * @author Gaetan Collaud
 * @param <T>
 */
public interface Observer<T> {

	void valueChanged(T newValue);
}
