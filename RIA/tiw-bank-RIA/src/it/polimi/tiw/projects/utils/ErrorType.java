package it.polimi.tiw.projects.utils;

public enum ErrorType {
	
	checkOk(0),
	negativeAmount(1),
	insufficientBalance(2),
	userDestNotMatchDestAccount(3),
	accountNotExists(4),
	idNotExists(5),
	selfTransfer(6);
	
	private int errorID;

	ErrorType(int errorID) { this.errorID = errorID; }
	
	public int getErrorType() { return errorID; }
}
