package com.dua3.utility.text;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.node.Visitor;

import com.dua3.utility.Pair;
import com.dua3.utility.text.TextAttributes.Attribute;

class RichTextRenderer {

    static class LiteralCollectingVisitor extends AbstractVisitor {
        private StringBuilder sb = new StringBuilder();

        @Override
        public String toString() {
            return sb.toString();
        }

        @Override
        public void visit(Text node) {
            sb.append(node.getLiteral());
            super.visit(node);
        }
    }

    static class RTVisitor extends AbstractVisitor {

        private final RichTextBuilder app;
        private boolean atStartOfLine = true;

        public RTVisitor(RichTextBuilder app) {
            this.app = app;
        }

        @Override
        public void visit(BlockQuote node) {
            Attribute attr = new Attribute(MarkDownStyle.BLOCK_QUOTE);
            appendNewLineIfNeeded();
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(BulletList node) {
            Attribute attr = new Attribute(MarkDownStyle.BULLET_LIST);
            appendNewLineIfNeeded();
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(Code node) {
            Attribute attr = new Attribute(MarkDownStyle.CODE);
            push(TextAttributes.STYLE_START_RUN, attr);
            appendText(node.getLiteral());
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        private void appendText(String literal) {
            app.append(literal);
            if (!literal.isEmpty()) {
                atStartOfLine = literal.charAt(literal.length()-1)=='\n';
            }
        }

        @Override
        public void visit(CustomBlock node) {
            Attribute attr = new Attribute(MarkDownStyle.CUSTOM_BLOCK);
            appendNewLineIfNeeded();
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(CustomNode node) {
            Attribute attr = new Attribute(MarkDownStyle.CUSTOM_NODE);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(Document node) {
            Attribute attr = new Attribute(MarkDownStyle.DOCUMENT);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(Emphasis node) {
            Attribute attr = new Attribute(MarkDownStyle.EMPHASIS);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(FencedCodeBlock node) {
            Attribute attr = new Attribute(MarkDownStyle.FENCED_CODE_BLOCK);
            push(TextAttributes.STYLE_START_RUN, attr);
            appendText(node.getLiteral());
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(HardLineBreak node) {
            Attribute attr = new Attribute(MarkDownStyle.HARD_LINE_BREAK);
            push(TextAttributes.STYLE_START_RUN, attr);
            appendText("\n");
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(Heading node) {
            String id = extractText(node, (v, n) -> v.visit(n)).toLowerCase(Locale.ROOT).trim();
            Attribute attr = new Attribute(MarkDownStyle.HEADING,
                    Pair.of(MarkDownStyle.ATTR_HEADING_LEVEL, node.getLevel()),
                    Pair.of(MarkDownStyle.ATTR_ID, id));
            appendNewLineIfNeeded();
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(HtmlBlock node) {
            Attribute attr = new Attribute(MarkDownStyle.HTML_BLOCK);
            push(TextAttributes.STYLE_START_RUN, attr);
            appendText(node.getLiteral());
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(HtmlInline node) {
            Attribute attr = new Attribute(MarkDownStyle.HTML_INLINE);
            push(TextAttributes.STYLE_START_RUN, attr);
            appendText(node.getLiteral());
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(Image node) {
            String altText = extractText(node, (v, n) -> v.visit(n));

            Attribute attr = new Attribute(MarkDownStyle.IMAGE,
                    Pair.of(MarkDownStyle.ATTR_IMAGE_SRC, node.getDestination()),
                    Pair.of(MarkDownStyle.ATTR_IMAGE_TITLE, node.getTitle()),
                    Pair.of(MarkDownStyle.ATTR_IMAGE_ALT, altText));

            push(TextAttributes.STYLE_START_RUN, attr);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(IndentedCodeBlock node) {
            Attribute attr = new Attribute(MarkDownStyle.INDENTED_CODE_BLOCK);
            push(TextAttributes.STYLE_START_RUN, attr);
            appendText(node.getLiteral());
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(Link node) {
            Attribute attr = new Attribute(MarkDownStyle.LINK,
                    Pair.of(MarkDownStyle.ATTR_LINK_HREF, node.getDestination()),
                    Pair.of(MarkDownStyle.ATTR_LINK_TITLE, node.getTitle()),
                    Pair.of(MarkDownStyle.ATTR_LINK_EXTERN, !node.getDestination().startsWith("#")));
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(ListItem node) {
            Attribute attr = new Attribute(MarkDownStyle.LIST_ITEM);
            appendNewLineIfNeeded();
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(OrderedList node) {
            Attribute attr = new Attribute(MarkDownStyle.ORDERED_LIST);
            appendNewLineIfNeeded();
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(Paragraph node) {
            Attribute attr = new Attribute(MarkDownStyle.PARAGRAPH);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendText("\n");
        }

        private void appendNewLineIfNeeded() {
            if (!atStartOfLine) {
                appendNewLine();
            }
        }

        private void appendNewLine() {
            app.append('\n');
            atStartOfLine = true;
        }

        @Override
        public void visit(SoftLineBreak node) {
            Attribute attr = new Attribute(MarkDownStyle.SOFT_LINE_BREAK);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            // TODO what to put here?
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(StrongEmphasis node) {
            Attribute attr = new Attribute(MarkDownStyle.STRONG_EMPHASIS);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
        }

        @Override
        public void visit(Text node) {
            appendText(node.getLiteral());
            super.visit(node);
        }

        @Override
        public void visit(ThematicBreak node) {
            Attribute attr = new Attribute(MarkDownStyle.THEMATIC_BREAK);
            push(TextAttributes.STYLE_START_RUN, attr);
            super.visit(node);
            push(TextAttributes.STYLE_END_RUN, attr);
            appendNewLineIfNeeded();
        }

        private <N extends Node> String extractText(N node, BiConsumer<? super Visitor, N> consumer) {
            LiteralCollectingVisitor lcv = new LiteralCollectingVisitor();
            consumer.accept(lcv, node);
            return lcv.toString();
        }

        private void push(String key, Attribute attr) {
            @SuppressWarnings("unchecked")
            List<Attribute> current = (List<Attribute>) app.pop(key);
            if (current == null) {
                current = new LinkedList<>();
            }
            current.add(attr);
            app.push(key, current);
        }

    }

    public RichText render(Node node) {
        RichTextBuilder app = new RichTextBuilder();
        render(node, app);
        return app.toRichText();
    }

    public void render(Node node, RichTextBuilder app) {
        node.accept(new RTVisitor(app));
    }
}
