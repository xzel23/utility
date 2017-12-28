package com.dua3.utility.swing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

import com.dua3.utility.Pair;
import com.dua3.utility.swing.StyledDocumentBuilder.Tag;

public class DocumentExt extends DefaultStyledDocument {
	private static final long serialVersionUID = 1L;
	
	private final List<Pair<Integer,Tag>> tags = new LinkedList<>();
	
	class ComponentData {

		private Map<String,ButtonGroup> buttonGroups = new HashMap<>();
		
		public ButtonGroup getButtonGroup(String name) {
			return buttonGroups.computeIfAbsent(name, n -> new ButtonGroup());
		}
		
	}

    public void insertTag(int pos, Tag tag) {
        tags.add(Pair.of(pos,tag));
    }

    public void setDocumentInto(JTextPane tp) {
    	ComponentData data = new ComponentData();
    	
        // sort tags by position (Note: sort() is stable)
        tags.sort((a,b) -> b.first-a.first);

        tp.setDocument(this);
        for (Pair<Integer,Tag> entry: tags) {
            tp.setCaretPosition(entry.first);
            tp.insertComponent(entry.second.createComponent(data));
        }
    }
}
