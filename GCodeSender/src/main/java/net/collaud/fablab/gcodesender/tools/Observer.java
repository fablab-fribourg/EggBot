package net.collaud.fablab.gcodesender.tools;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 * @param <T>
 */
public interface Observer<T> {

	void valueChanged(T newValue);
}
