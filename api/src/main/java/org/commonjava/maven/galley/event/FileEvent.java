/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;

public class FileEvent
{

    private final Transfer transfer;

    protected FileEvent( final Transfer transfer )
    {
        this.transfer = transfer;
    }

    public Transfer getTransfer()
    {
        return transfer;
    }

    public String getExtraInfo()
    {
        return "";
    }

    @Override
    public String toString()
    {
        return String.format( "%s [extra-info=%s, transfer=%s]", getClass().getSimpleName(), getExtraInfo(), transfer );
    }

}
