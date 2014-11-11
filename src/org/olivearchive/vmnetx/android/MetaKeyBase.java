/**
 * Copyright (C) 2012 Iordan Iordanov
 * Copyright (C) 2009 Michael A. MacDonald
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

package org.olivearchive.vmnetx.android;

/**
 * @author Michael A. MacDonald
 *
 */
public class MetaKeyBase implements Comparable<MetaKeyBase> {
    final int keySym;
    final int keyEvent;
    final int mouseButtons;
    final String name;
    final boolean isMouse;
    final boolean isKeyEvent;
    
    MetaKeyBase(int mouseButtons, String name)
    {
        this.keySym = 0;
        this.keyEvent = 0;
        this.mouseButtons = mouseButtons;
        this.name = name;
        this.isMouse = true;
        this.isKeyEvent = false;
    }
    
    MetaKeyBase(String name, int keySym, int keyEvent)
    {
        this.keySym = keySym;
        this.keyEvent = keyEvent;
        this.mouseButtons = 0;
        this.name = name;
        this.isMouse = false;
        this.isKeyEvent = true;
    }
    
    MetaKeyBase(String name, int keySym)
    {
        this.keySym = keySym;
        this.keyEvent = 0;
        this.mouseButtons = 0;
        this.name = name;
        this.isMouse = false;
        this.isKeyEvent = false;        
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MetaKeyBase another) {
        return name.compareTo(another.name);
    }
}
