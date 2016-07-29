package org.jeromerodrigo.lucidengine.entity;

import org.dyn4j.geometry.Vector2;
import org.jeromerodrigo.lucidengine.Drawable;
import org.jeromerodrigo.lucidengine.Updateable;

public interface Entity extends Drawable, Updateable {

	Vector2 getMovement();

	double getX();

	double getY();

}
