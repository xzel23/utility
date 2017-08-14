package com.dua3.utility.text;

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

class RichTextRenderer {

	static class RTVisitor extends AbstractVisitor {

		private final RichTextBuilder app;

		public RTVisitor(RichTextBuilder app) {
			this.app=app;
		}

		@Override
		public void visit(BlockQuote node) {
			app.push(RichText.STYLE, MarkDownStyle.BLOCK_QUOTE.name());
			super.visit(node);
		}

		@Override
		public void visit(BulletList node) {
			app.push(RichText.STYLE, MarkDownStyle.BULLET_LIST.name());
			super.visit(node);
		}

		@Override
		public void visit(Code node) {
			app.push(RichText.STYLE, MarkDownStyle.CODE.name());
			super.visit(node);
		}

		@Override
		public void visit(Document node) {
			app.push(RichText.STYLE, MarkDownStyle.DOCUMENT.name());
			super.visit(node);
		}

		@Override
		public void visit(Emphasis node) {
			app.push(RichText.STYLE, MarkDownStyle.EMPHASIS.name());
			super.visit(node);
		}

		@Override
		public void visit(FencedCodeBlock node) {
			app.push(RichText.STYLE, MarkDownStyle.FENCED_CODE_BLOCK.name());
			super.visit(node);
		}

		@Override
		public void visit(HardLineBreak node) {
			app.push(RichText.STYLE, MarkDownStyle.HARD_LINE_BREAK.name());
			super.visit(node);
		}

		@Override
		public void visit(Heading node) {
			app.push(RichText.STYLE, MarkDownStyle.HEADING.name());
			super.visit(node);
		}

		@Override
		public void visit(ThematicBreak node) {
			app.push(RichText.STYLE, MarkDownStyle.THEMATIC_BREAK.name());
			super.visit(node);
		}

		@Override
		public void visit(HtmlInline node) {
			app.push(RichText.STYLE, MarkDownStyle.HTML_INLINE.name());
			super.visit(node);
		}

		@Override
		public void visit(HtmlBlock node) {
			app.push(RichText.STYLE, MarkDownStyle.HTML_BLOCK.name());
			super.visit(node);
		}

		@Override
		public void visit(Image node) {
			app.push(RichText.STYLE, MarkDownStyle.IMAGE.name());
			super.visit(node);
		}

		@Override
		public void visit(IndentedCodeBlock node) {
			app.push(RichText.STYLE, MarkDownStyle.INDENTED_CODE_BLOCK.name());
			super.visit(node);
		}

		@Override
		public void visit(Link node) {
			app.push(RichText.STYLE, MarkDownStyle.LINK.name());
			super.visit(node);
		}

		@Override
		public void visit(ListItem node) {
			app.push(RichText.STYLE, MarkDownStyle.LIST_ITEM.name());
			super.visit(node);
		}

		@Override
		public void visit(OrderedList node) {
			app.push(RichText.STYLE, MarkDownStyle.ORDERED_LIST.name());
			super.visit(node);
		}

		@Override
		public void visit(Paragraph node) {
			app.push(RichText.STYLE, MarkDownStyle.PARAGRAPH.name());
			super.visit(node);
		}

		@Override
		public void visit(SoftLineBreak node) {
			app.push(RichText.STYLE, MarkDownStyle.SOFT_LINE_BREAK.name());
			super.visit(node);
		}

		@Override
		public void visit(StrongEmphasis node) {
			app.push(RichText.STYLE, MarkDownStyle.STRONG_EMPHASIS.name());
			super.visit(node);
		}

		@Override
		public void visit(Text node) {
			app.push(RichText.STYLE, MarkDownStyle.TEXT.name());
            app.append(node.getLiteral());
			super.visit(node);
		}

		@Override
		public void visit(CustomBlock node) {
			app.push(RichText.STYLE, MarkDownStyle.CUSTOM_BLOCK.name());
			super.visit(node);
		}

		@Override
		public void visit(CustomNode node) {
			app.push(RichText.STYLE, MarkDownStyle.CUSTOM_NODE.name());
			super.visit(node);
		}

	}

	public void render(Node node, RichTextBuilder app) {
		node.accept(new RTVisitor(app));
	}

	public RichText render(Node node) {
		RichTextBuilder app = new RichTextBuilder();
		render(node, app);
		return app.toRichText();
	}
}
