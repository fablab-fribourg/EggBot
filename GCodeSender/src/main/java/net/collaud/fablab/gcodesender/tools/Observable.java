package net.collaud.fablab.gcodesender.tools;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 * @param <T>
 */
public class Observable<T> {
	private final List<Observer<T>> observer;

	public Observable() {
		observer = new ArrayList<>();
	}
	
	public void addObserver(Observer<T> obs){
		observer.add(obs);
	}
	
	public void notifyObservers(T newValue){
		observer.forEach(o -> o.valueChanged(newValue));
	}
}
