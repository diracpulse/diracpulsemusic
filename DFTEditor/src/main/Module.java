package main;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashSet;

public interface Module {
	
	public enum ConnectorType {
		INPUT,
		OUTPUT,
	}
	
	abstract void mousePressed(int x, int y);
	abstract void draw(Graphics g, int startX, int startY);
	abstract int getWidth();
	abstract int getHeight();
	abstract long getModuleId();
	
	public interface Connector {
		
		abstract Rectangle getSelectArea();
		abstract Long getConnectorID();
		abstract ConnectorType getConnectorType();
		abstract void setConnection(Long connectionID);
		abstract Long getConnection();
		abstract void removeConnection();
		abstract Module getParent();
	}
	
	public abstract class Output implements Connector {
		
		Rectangle selectArea = null;
		Long connectionID = null;
		ConnectorType connectionType = ConnectorType.OUTPUT;
		Long connectedTo = null;
		Module parent = null;
		
		public Output(Module parent, Rectangle selectArea, Long connectionID) {
			this.parent = parent;
			this.selectArea = selectArea;
			this.connectionID = connectionID;
		}
		
		public Rectangle getSelectArea() {
			return selectArea;
		}
		
		public Long getConnectorID() {
			return connectionID;
		}
		
		public ConnectorType getConnectorType() {
			return connectionType;
		}
		
		public void setConnection(Long connectedTo) {
			this.connectedTo = connectedTo;
		}
		
		public Long getConnection() {
			return this.connectedTo;
		}
		
		public void removeConnection() {
			this.connectedTo = null;
		}
		
		public Module getParent() {
			return parent;
		}
		
		public abstract double[] getSamples(HashSet<Long> waitingForModuleIDs);
		
	}
	
	public abstract class Input implements Connector {
		
		Rectangle selectArea = null;
		Long connectionID = null;
		ConnectorType connectionType = ConnectorType.INPUT;
		Long connectedFrom = null;
		Module parent = null;
		
		public Input(Module parent, Rectangle selectArea, Long connectionID) {
			this.parent = parent;
			this.selectArea = selectArea;
			this.connectionID = connectionID;
		}
		
		public Rectangle getSelectArea() {
			return selectArea;
		}
		
		public Long getConnectorID() {
			return connectionID;
		}
		
		public ConnectorType getConnectorType() {
			return connectionType;
		}
		
		public void setConnection(Long connectedFrom) {
			this.connectedFrom = connectedFrom;
		}
		
		public Long getConnection() {
			return this.connectedFrom;
		}
		
		public void removeConnection() {
			this.connectedFrom = null;
		}
		
		public Module getParent() {
			return parent;
		}
	}
	
}
