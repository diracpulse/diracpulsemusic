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
	abstract Integer getModuleId();
	
	public interface Connector {
		
		abstract Rectangle getSelectArea();
		abstract Integer getConnectorID();
		abstract ConnectorType getConnectorType();
		abstract void setConnection(Integer connectionID);
		abstract Integer getConnection();
		abstract void removeConnection();
		abstract Module getParent();
	}
	
	public abstract class Output implements Connector {
		
		Rectangle selectArea = null;
		Integer connectionID = null;
		ConnectorType connectionType = ConnectorType.OUTPUT;
		Integer connectedTo = null;
		Module parent = null;
		
		public Output(Module parent, Rectangle selectArea, Integer connectionID) {
			this.parent = parent;
			this.selectArea = selectArea;
			this.connectionID = connectionID;
		}
		
		public Rectangle getSelectArea() {
			return selectArea;
		}
		
		public Integer getConnectorID() {
			return connectionID;
		}
		
		public ConnectorType getConnectorType() {
			return connectionType;
		}
		
		public void setConnection(Integer connectedTo) {
			this.connectedTo = connectedTo;
		}
		
		public Integer getConnection() {
			return this.connectedTo;
		}
		
		public void removeConnection() {
			this.connectedTo = null;
		}
		
		public Module getParent() {
			return parent;
		}
		
		public abstract double[] getSamples(HashSet<Integer> waitingForModuleIDs);
		
		public abstract void clearSamples();
		
	}
	
	public abstract class Input implements Connector {
		
		Rectangle selectArea = null;
		Integer connectionID = null;
		ConnectorType connectionType = ConnectorType.INPUT;
		Integer connectedFrom = null;
		Module parent = null;
		
		public Input(Module parent, Rectangle selectArea, Integer connectionID) {
			this.parent = parent;
			this.selectArea = selectArea;
			this.connectionID = connectionID;
		}
		
		public Rectangle getSelectArea() {
			return selectArea;
		}
		
		public Integer getConnectorID() {
			return connectionID;
		}
		
		public ConnectorType getConnectorType() {
			return connectionType;
		}
		
		public void setConnection(Integer connectedFrom) {
			this.connectedFrom = connectedFrom;
		}
		
		public Integer getConnection() {
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
