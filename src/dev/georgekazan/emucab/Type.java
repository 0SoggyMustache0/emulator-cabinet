package dev.georgekazan.emucab;

public enum Type {

	NES("nes"), GBA("gba"), NDS("nds"), SNES("sfc"), PS("img");
	
	private String extension;
	
	private Type(String extension) {
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension;
	}
}
