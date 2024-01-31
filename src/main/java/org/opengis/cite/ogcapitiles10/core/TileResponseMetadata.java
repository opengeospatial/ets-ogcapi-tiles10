package org.opengis.cite.ogcapitiles10.core;

public class TileResponseMetadata {

	private boolean allRequestsSuccessful = true;

	private int responseCode = -1;

	public TileResponseMetadata(boolean allRequestsSuccessful, int responseCode) {
		this.allRequestsSuccessful = allRequestsSuccessful;
		this.responseCode = responseCode;
	}

	public boolean areAllRequestsSuccessful() {
		return allRequestsSuccessful;
	}

	public void setAllRequestsSuccessful(boolean allRequestsSuccessful) {
		this.allRequestsSuccessful = allRequestsSuccessful;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

}
