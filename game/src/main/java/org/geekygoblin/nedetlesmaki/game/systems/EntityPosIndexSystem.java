/*
 * Copyright © 2013, Pierre Marijon <pierre@marijon.fr>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * The Software is provided "as is", without warranty of any kind, express or 
 * implied, including but not limited to the warranties of merchantability, 
 * fitness for a particular purpose and noninfringement. In no event shall the 
 * authors or copyright holders X be liable for any claim, damages or other 
 * liability, whether in an action of contract, tort or otherwise, arising from,
 * out of or in connection with the software or the use or other dealings in the
 * Software.
 */
package org.geekygoblin.nedetlesmaki.game.systems;

import java.util.Stack;

import com.artemis.Entity;

import org.geekygoblin.nedetlesmaki.game.components.EntityPosIndex;

/**
 *
 * @author natir
 */
public class EntityPosIndexSystem {
    
    private EntityPosIndex index;
    private Stack<EntityPosIndex> oldIndex;
    private static EntityPosIndexSystem instance = null;
    
    private EntityPosIndexSystem(EntityPosIndex index) {
	    this.index = index;
	    this.oldIndex = new Stack();
	}

    public static EntityPosIndexSystem getInstance(EntityPosIndex index) {
	if (instance == null) {
	    instance = new EntityPosIndexSystem(index);
	}
	
	return instance;
    }

    public boolean saveWorld() {
	this.oldIndex.push(this.index.clone());
        return true;
    }

    public boolean addEntity(int x, int y, Entity eId) {
	if(x > 15 || x < 0 || y > 15 || y < 0) {
	    return false;
	}

        index.setEntityWithPos(x, y, eId);
	return true;
    }

    public boolean removeEntity(int x, int y) {
	if(x > 15 || x < 0 || y > 15 || y < 0) {
	    return false;
	}
	
	this.addEntity(x, y, null);
	return true;
    }

    public Entity getEntity(int x, int y) {
	if(x > 15 || x < 0 || y > 15 || y < 0) {
	    return null;
	}
	return index.getEntityWithPos(x, y);
    }

    public boolean moveEntity(int x1, int y1, int x2, int y2) {
	Entity tmpE = index.getEntityWithPos(x1, y1);
	if(this.addEntity(x2, y2, tmpE)) {
	    this.removeEntity(x1, y1);
	    return true;
	}

	return false;
    }

    public int sizeOfStack() {
	return this.oldIndex.size();
    }

    public EntityPosIndex getLastWorld() {
	return this.oldIndex.peek();
    }

    public EntityPosIndex getThisWorld() {
	return this.index;
    }
}
