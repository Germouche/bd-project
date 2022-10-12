package gange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class Category {

	private final String name;
	private final Category parent;
	private HashMap<String,Category> childen = new HashMap<String,Category>();
	private final boolean first; // Si la catégorie est de premier rang (sans parent)
	private final ArrayList<String> products = new ArrayList<String>();
	
	public Category(String name, Category parent, boolean first) {
		this.name = name;
		this.parent = parent;
		this.first = first;
	}
	
	public void fillCategory(Iterator<Category> it) {
		this.childen.clear();
		Category cat;
		while (it.hasNext()) {
			cat = it.next();
			this.childen.put(cat.getName(), cat);
		  }
	}
	
	public String getName() {
		return name;
	}

	public void fillProducts(Iterator<String> it) {
		this.products.clear();
		while (it.hasNext()) {
			this.products.add(it.next());
		  }
	}
	
	public boolean hasInfos() {
		return !(this.childen.isEmpty()|this.products.isEmpty());
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	public boolean containsChild(String catName) {
		return this.childen.containsKey(catName);
	}
	
	public Category getChild(String catName) {
		return this.childen.get(catName);
	}
	
	public String categoryInfos() {
		String str = "";
		if (!this.first) {
			str += "Catégories : actuelle : " + this.name;
			str += "\nprécédante : " + this.parent + "\n";
			str += "Sous-catégories :";
		} else {
			str += "Catégories racines :";
		}
		for (Entry<String,Category> child : this.childen.entrySet()) {
			str += "  " + child.getValue().getName();
		}
		str += "\n";
		return str;
	}
	
	public String ProductInfos() {
		String str = "";
		if (!this.products.isEmpty()) {
			str += "Produits : ";
			for (String product : this.products) {
				str += product + "  ";
			}
		}
		str += "\n";
		return str;
	}
	
	public boolean isFirst() {
		return this.first;
	}
	
}
