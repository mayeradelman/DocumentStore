package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;

class DocumentProperties implements Comparable<DocumentProperties>{
	URI uri;
	long lastUsedTime;
	DocumentProperties(URI uri, long timeInNanoSeconds){
		this.uri = uri;
		this.lastUsedTime = timeInNanoSeconds;
	}
	URI getURI(){
		return this.uri;
	}
	long getLastUseTime(){
		return this.lastUsedTime;
	}

	@Override
	public int compareTo(DocumentProperties that) {
		return Long.compare(this.getLastUseTime(), that.getLastUseTime());
	}
}
