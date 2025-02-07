/*
	Copyright 2009 Adam Ribaldo 
	 
	Developed by Adam Ribaldo, Chris Lloyd
    
    This file is part of SevenUpLive.
    http://www.makingthenoise.com/sevenup/

    SevenUpLive is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SevenUpLive is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with SevenUpLive.  If not, see <http://www.gnu.org/licenses/>.
*/

package mtn.sevenuplive.max.mxj;


public class SevenUp4LiveStepperClient extends SevenUp4LiveMidiClient {
	
	public SevenUp4LiveStepperClient(SevenUp4Live app, int instanceNum, int ch) {
		super(app, instanceNum, ch);
	}
	
	protected int getOutletOrdinal() {
		return SevenUp4Live.eOutlets.StepperMidiOutlet.ordinal();
	}
	
}
