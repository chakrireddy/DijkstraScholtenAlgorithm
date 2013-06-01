package ds;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int timestamp = -1;
	Node senderNode = null;

	public Message(int timestamp, Node senderNode) {
		this.timestamp = timestamp;
		this.senderNode = senderNode;
	}
}
