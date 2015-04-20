package mathe172.minecraft.plugins.TeleParticles;

public class TPCachedCmd {
	public cmdId command;
	public String dataKey;
	public Object dataValue;
	
	public TPCachedCmd(cmdId command, String dataKey, Object dataValue) {
		this.command = command;
		this.dataKey = dataKey;
		this.dataValue = dataValue;
	}
	
	public static enum cmdId {
		delCmd(0), modCmd(1), delLoc(2), modLoc(3);
		
		private int intCode;
		
		private cmdId(int intCode) {
			this.intCode = intCode;
		}

		public int toInt() {
			return intCode;
		}
	}
}
