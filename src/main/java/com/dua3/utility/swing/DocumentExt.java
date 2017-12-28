package com.dua3.utility.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.swing.StyledDocumentBuilder.Tag;

public class DocumentExt extends DefaultStyledDocument {
	private static final long serialVersionUID = 1L;
	
    private static final Logger LOG = LoggerFactory.getLogger(DocumentExt.class);
    
	private final List<Pair<Integer,Tag>> tags = new LinkedList<>();
	private Map<String,Object> data = new HashMap<>();

	public void set(String var, Object value) {
		Object old = data.put(var, value);
		LOG.debug("\"{}\" -> {} [was: {}]", var, value, old);
	}
	
	public Object get(String var) {
		return data.get(var);
	}
	
	public Object getOrDefault(String var, Object dflt) {
		return data.getOrDefault(var, dflt);
	}
	
	class ComponentData {

		private Map<String,JComponent> components = new HashMap<>();
		private Map<String,ButtonGroup> buttonGroups = new HashMap<>();
		
		public ButtonGroup getButtonGroup(String name) {
			return buttonGroups.computeIfAbsent(name, n -> new ButtonGroup());
		}
		
		public DocumentExt getDocument() {
			return DocumentExt.this;
		}
		
		public void addComponent(String id, String name, JTextField component) {
			component.addActionListener(e -> {
				getDocument().set(name, component.getText());
			});
			component.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					getDocument().set(name, component.getText());
				}				
				@Override
				public void focusGained(FocusEvent e) {
					// nop
				}
			});
			addComponentRef(id, component);
		}

		private void addComponentRef(String id, JComponent component) {
			if (id!=null&&!id.isEmpty()) {
				JComponent old = components.put(id, component);
				LangUtil.check(old==null, "multiple components with the same ID: %s", id);
			}
		}
		
		public void addComponent(String id, String name, JCheckBox component) {
			component.addActionListener(e -> {
				getDocument().set(name, component.isSelected());
			});
			addComponentRef(id, component);
		}
		
		public void addComponent(String id, String name, JRadioButton component) {
			ButtonGroup group = getButtonGroup(id);
			group.add(component);
			component.addActionListener(e -> {
				ButtonModel selection = group.getSelection();
				Object value = selection==null?null:selection.getActionCommand();
				getDocument().set(name, value);
			});
			addComponentRef(id, component);
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
