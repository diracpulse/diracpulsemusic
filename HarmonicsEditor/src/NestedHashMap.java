import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;


public class NestedHashMap {
	
	private HashMap<Integer, NestedHashMap> data = null; 
	
	public NestedHashMap() {
		data = new HashMap<Integer, NestedHashMap>();
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
	
	public ArrayList<Integer> getRandomArray() {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		int numKeys = data.size();
		if(numKeys == 0) return returnVal;
		int keyIndex = HarmonicsEditor.randomGenerator.nextInt(data.size());
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
		int keyIndex = HarmonicsEditor.randomGenerator.nextInt(data.size());
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
