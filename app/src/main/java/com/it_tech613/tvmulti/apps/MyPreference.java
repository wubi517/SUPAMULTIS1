package com.it_tech613.tvmulti.apps;

import android.content.Context;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MyPreference {

	private Map<String,Object> values=new HashMap<String, Object>();
	private String name;
	private String path;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public MyPreference(Context context, String name){
		this.name=name;
		this.path=context.getApplicationInfo().dataDir+"/mytv";
		File f=new File(this.path);
		if(!f.exists())f.mkdir();
		f=new File(path+"/"+name+".z");
		if(f.exists())load();
		else save();
	}
	
	public void load(){
		try {
			ois=new ObjectInputStream(new FileInputStream(new File(path+"/"+name+".z")));
			values=(Map<String, Object>) ois.readObject();
			ois.close();
		} catch (IOException e) {
			values=new HashMap<String, Object>();
		}catch (ClassNotFoundException e) {
			values=new HashMap<String, Object>();
		}
	}
	public void put(String key, Object value){
		if(values==null)values=new HashMap();
		if(values.containsKey(key))values.remove(key);
		values.put(key, value);
		save();
	}
	
	public void save(){
		try {
			oos=new ObjectOutputStream(new FileOutputStream(new File(path+"/"+name+".z")));
			oos.writeObject(values);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean containsKey(String key){
		if(values==null)return false;
		return values.containsKey(key);
	}
	
	public Object get(String key){
		if(values==null)return null;
		return values.get(key);
	}
	
	public void remove(String key){
		if(values!=null){
			values.remove(key);save();
		}
	}
	
	public void removeAll(){
		this.values.clear();save();
	}
}
