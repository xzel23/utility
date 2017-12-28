package com.dua3.utility.swing;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

import com.dua3.utility.Pair;
import com.dua3.utility.swing.StyledDocumentBuilder.Tag;

public class DocumentExt extends DefaultStyledDocument {

    private final List<Pair<Integer,Tag>> tags = new LinkedList<>();

    public void insertTag(int pos, Tag tag) {
        tags.add(Pair.of(pos,tag));
    }

    public void setDocumentInto(JTextPane tp) {
        // sort tags by position (Note: sort() is stable)
        tags.sort((a,b) -> b.first-a.first);

        tp.setDocument(this);
        for (Pair<Integer,Tag> entry: tags) {
            tp.setCaretPosition(entry.first);
            tp.insertComponent(entry.second.createComponent());
        }
    }
}
