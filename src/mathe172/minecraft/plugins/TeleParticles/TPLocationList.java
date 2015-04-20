package mathe172.minecraft.plugins.TeleParticles;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TPLocationList extends LinkedHashMap<String, ArrayList<TPLocation>> {
	private static final long serialVersionUID = 2387302904008548585L;
	
	public boolean hasFrom;
	public boolean hasTo;
	
	public TPLocationList(boolean hasFrom, boolean hasTo) {
		super();
		this.hasFrom = hasFrom;
		this.hasTo = hasTo;
	}
		
}
