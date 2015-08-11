package org.genia.terminchecker;

public enum VisaType {
	GUEST(357, 584),
	NATIONAL(561, 906);
	
	public int realmId;
	public int categoryId;
	
	private VisaType(int realmId, int categoryId) {
		this.realmId = realmId;
		this.categoryId = categoryId;
	}
}

