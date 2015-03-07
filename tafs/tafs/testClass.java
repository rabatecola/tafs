package tafs;

import java.io.Serializable;

public class testClass implements Serializable
{
	public String	myName;
	public String	mySecondString;
	public Integer	myInteger;
	public byte[]	myPayload = null;

	public testClass()
	{
	}

	public testClass(String inName, String inSS, Integer inInteger)
	{
		myName = inName;
		mySecondString = inSS;
		myInteger = inInteger;
	}
}
