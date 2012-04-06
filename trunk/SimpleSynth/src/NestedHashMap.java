import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;


public class NestedHashMap {
	
	private HashMap<Integer, NestedHashMap> data = null; 
	
	public NestedHashMap() {
		data = new HashMap<Integer, NestedHashMap>();
	}
	
	public boolean isEmpty() {
		if(data == null) return true;
		if(data.size() == 0) return true;
		return false;
	}
	
	public void addArray(int[] array) {
		if(array == null) {
			System.out.println("Error: NestedTreeMap.addArray: null array");
		}
		int key = array[0];
		if(array.length == 1) {
			data.put(key, null);
			return;
		}
		if(!data.containsKey(key)) data.put(key, new NestedHashMap());
		int[] subArray = new int[array.length - 1];
		for(int index = 1; index < array.length; index++) {
			subArray[index - 1] = array[index];
		}
		data.get(key).addArray(subArray);
	}
	
	public boolean removeArray(int[] array) {
		return removeArray(array, 0);
	}
	
	// returns true if key in current level does not have children
	public boolean removeArray(int[] array, int index) {
		if(index > array.length - 1) {
			System.out.println("NestedTreeMap.removeArray: Array too long");
			return false;
		}
		int key = array[index];
		if(!data.containsKey(key)) {
			System.out.println("NestedTreeMap.removeArray: Array does not exist in map");
			return false;
		}
		if(isTerminal(key)) {
			//System.out.println("NestedTreeMap.removeArray: isTerminal ");
			//printArray(array);
			data.remove(key);
			if(data.isEmpty()) return true;
			return false;
		}
		if(data.get(key).removeArray(array, index + 1)) {
			//System.out.println("NestedTreeMap.removeArray: nested call " + index);
			data.remove(key);
			if(data.isEmpty()) return true;
			return false;
		}
		//System.out.println("NestedTreeMap.removeArray: should not reach end of recursive call");
		return false;
	}
	
	public static void printArray(int[] array) {
		for(int index: array) System.out.print(index + " ");
		System.out.println();
	}

	public ArrayList<Integer> getRandomArray() {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		int numKeys = data.size();
		if(numKeys == 0) return returnVal;
		int keyIndex = SimpleSynth.randomGenerator.nextInt(data.size());
		int key = getAvailableKeys().get(keyIndex);
		if(isTerminal(key)) {
			data.remove(key);
			returnVal.add(key);
		} else {
			returnVal = data.get(key).getRandomArray(this, key);
			returnVal.add(0, key);
		}
		return returnVal;
	}
	
	private ArrayList<Integer> getRandomArray(NestedHashMap caller, int prevKey) {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		int numKeys = data.size();
		if(numKeys == 0) {
			caller.makeTerminal(prevKey);
			return returnVal;
		}
		int keyIndex = SimpleSynth.randomGenerator.nextInt(data.size());
		int key = getAvailableKeys().get(keyIndex);
		if(isTerminal(key)) {
			data.remove(key);
			returnVal.add(key);
		} else {
			returnVal = data.get(key).getRandomArray(this, key);
			returnVal.add(0, key);
		}
		return returnVal;
	}
	
	public void makeTerminal(int keyVal) {
		data.put(keyVal, null);
	}
	
	public ArrayList<Integer> getAvailableKeys() {
		return new ArrayList<Integer>(data.keySet());
	}
	
	public boolean isTerminal(int key) {
		if(data.get(key) == null) return true;
		return false;
	}
	
}
