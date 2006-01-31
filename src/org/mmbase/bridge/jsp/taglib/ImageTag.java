/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import java.io.File;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.util.functions.*;
import org.mmbase.util.images.*;
import org.mmbase.util.UriParser;
import org.mmbase.module.builders.Images;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Produces an url to the image servlet mapping. Using this tag makes
 * your pages more portable to other system, and hopefully less
 * sensitive for future changes in how the image servlet works.
 *
 * @author Michiel Meeuwissen
 * @version $Id: ImageTag.java,v 1.68 2006-01-31 14:34:11 michiel Exp $
 */

public class ImageTag extends FieldTag {

    private static final Logger log = Logging.getLoggerInstance(ImageTag.class);

    public static final int MODE_URL = 0;
    public static final int MODE_HTML_ATTRIBUTES = 1;
    public static final int MODE_HTML_IMG = 2;

    public static final String CROP_BEGIN = "begin";
    public static final String CROP_MIDDLE = "middle";
    public static final String CROP_END = "end";


    private static Boolean makeRelative = null;
    private static Boolean urlConvert   = null;

    /** Holds value of property template. */
    private Attribute template = Attribute.NULL;

    /** Holds value of property mode. */
    private Attribute mode = Attribute.NULL;

    /** Holds value of property width. */
    private Attribute width = Attribute.NULL;
    
    /** Holds value of property height. */
    private Attribute height = Attribute.NULL;
    
    /** Holds value of property crop. */
    private Attribute crop = Attribute.NULL;
    
    /** Holds value of property style. */
    private Attribute style = Attribute.NULL;
    
    /** Holds value of property clazz. */
    private Attribute styleClass = Attribute.NULL;
    
    /** Holds value of property align. */
    private Attribute align = Attribute.NULL;
    
    /** Holds value of property border. */
    private Attribute border = Attribute.NULL;
    
    /** Holds value of property hspace. */
    private Attribute hspace = Attribute.NULL;
    
    /** Holds value of property vspace. */
    private Attribute vspace = Attribute.NULL;

    
    private Object prevDimension;

    /**
     * The transformation template
     */
    public void setTemplate(String t) throws JspTagException {
        template = getAttribute(t);
    }

    public void setMode(String m) throws JspTagException {
        mode = getAttribute(m);
    }

    public void setAlign(String align) throws JspTagException {
        this.align = getAttribute(align);
    }

    
    public void setBorder(String border) throws JspTagException {
        this.border = getAttribute(border);
    }

    
    public void setStyleClass(String styleClass) throws JspTagException {
        this.styleClass = getAttribute(styleClass);
    }

    
    public void setCrop(String crop) throws JspTagException {
        this.crop = getAttribute(crop);
    }

    
    public void setHeight(String height) throws JspTagException {
        this.height = getAttribute(height);
    }

    
    public void setHspace(String hspace) throws JspTagException {
        this.hspace = getAttribute(hspace);
    }
    
    public void setStyle(String style) throws JspTagException {
        this.style = getAttribute(style);
    }

    
    public void setVspace(String vspace) throws JspTagException {
        this.vspace = getAttribute(vspace);
    }

    
    public void setWidth(String width) throws JspTagException {
        this.width = getAttribute(width);
    }

    private int getMode() throws JspTagException {
        String m = mode.getString(this).toLowerCase();
        if (m.equals("") || m.equals("url")) {
            return MODE_URL;
        } else if (m.equals("attributes")) {
            return MODE_HTML_ATTRIBUTES;
        } else if (m.equals("img")) {
            return MODE_HTML_IMG;
        } else {
            throw new JspTagException("Value '" + m + "' not known for 'mode' attribute");
        }
    }


    private String getCrop() throws JspTagException {
        String m = crop.getString(this).toLowerCase();
        if (m.equals("")) { 
            return null;
        } else if (m.equals("middle")) {
            return CROP_MIDDLE;
        } else if (m.equals("begin")) {
            return CROP_BEGIN;
        } else if (m.equals("end")) {
            return CROP_END;
        } else {
            throw new JspTagException("Value '" + m + "' not known for 'crop' attribute");
        }
    }

    private boolean makeRelative() {
        if (makeRelative == null) {            
            String setting = pageContext.getServletContext().getInitParameter("mmbase.taglib.url.makerelative");
            makeRelative = Boolean.valueOf("true".equals(setting));
        }
        return makeRelative.booleanValue();
    }

    protected Node getServletNode(Node node, String template) {
        if (urlConvert() || "".equals(template)) {
            return node;
        } else {
            // the cached image
            return node.getFunctionValue("cachednode", new Parameters(Images.CACHE_PARAMETERS).set("template", template)).toNode();
        }

    }

    private boolean urlConvert() {
        if (urlConvert == null) {
            urlConvert = Boolean.valueOf("true".equals(pageContext.getServletContext().getInitParameter("mmbase.taglib.image.urlconvert")));
        }
        return urlConvert.booleanValue();
    }

    public int doStartTag() throws JspTagException {
        Node node = getNode();
        if (!node.getNodeManager().hasField("handle")) {
            throw new JspTagException("Found parent node '" + node.getNumber() + "' of type " + node.getNodeManager().getName() + " does not have 'handle' field, therefore cannot be a image. Perhaps you have the wrong node, perhaps you'd have to use the 'node' attribute?");
        }

        helper.useEscaper(false);
        prevDimension = pageContext.getAttribute("dimension");

        String templateStr = getTemplate(node, template.getString(this), width.getInt(this, 0), height.getInt(this, 0), getCrop());
        Dimension dim = getDimension(node, templateStr);

        node = getServletNode(node, templateStr);

        String servletArgument = getServletArgument(node, templateStr);

        String servletPath = getServletPath(node, servletArgument);
        String outputValue = getOutputValue(getMode(), node, servletPath, dim);

        if (outputValue != null) {
            helper.setValue(outputValue);
        }
        pageContext.setAttribute("dimension", dim);

        if (getId() != null) {
            getContextProvider().getContextContainer().register(getId(), helper.getValue());
        }

        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspTagException {
        if (prevDimension == null) {
            pageContext.removeAttribute("dimension");
        } else {
            pageContext.setAttribute("dimension", prevDimension);
        }
        helper.doEndTag();
        return super.doEndTag();
    }

    public String getServletArgument(Node node, String t) {
        String servletArgument; // can be the node-number or a template (if that is configured to be allowed).
        if ("".equals(t) || ! urlConvert()) {
            // the node/image itself
            servletArgument = node.getStringValue("number");
        } else {
            try {                    
                servletArgument = "" + node.getNumber() + "+" + java.net.URLEncoder.encode(t, "UTF-8");
            } catch (java.io.UnsupportedEncodingException uee) {
                // cannot happen 'UTF-8' is supported.
                servletArgument = "" + node.getNumber() + "+" + t;
            }
        }
        return servletArgument;
    }

    public String getServletPath(Node node, String servletArgument) throws JspTagException {
        Function servletPathFunction = getServletFunction(node);        
        Parameters args = getServletArguments(servletArgument, servletPathFunction);
        fillStandardParameters(args);
        return servletPathFunction.getFunctionValue( args).toString();
    }

    public Function getServletFunction(Node node) {
        Function servletPathFunction = node.getFunction("servletpath");
        return servletPathFunction;
    }

    public Parameters getServletArguments(String servletArgument, Function servletPathFunction) {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();        
        Parameters args = servletPathFunction.createParameters();
        args.set("context",  makeRelative() ? UriParser.makeRelative(new File(req.getServletPath()).getParent(), "/") : req.getContextPath())
            .set("argument", servletArgument);
        return args;
    }

    public String getOutputValue(int mode, Node node, String servletPath, Dimension dim) throws JspTagException {
        String outputValue = null;
        switch(mode) {
        case MODE_URL: 
            outputValue = ((HttpServletResponse) pageContext.getResponse()).encodeURL(servletPath);
            break;
        case MODE_HTML_ATTRIBUTES: {
            String url = ((HttpServletResponse) pageContext.getResponse()).encodeURL(servletPath);
            if (dim.getHeight() > 0 && dim.getWidth() > 0) {
                outputValue = getBaseAttributes(url, dim);
            } else {
                log.warn("Found odd dimension " + dim);
                outputValue = getSrcAttribute(url);
            }
            break;
        }
        case MODE_HTML_IMG: {
            String url = ((HttpServletResponse) pageContext.getResponse()).encodeURL(servletPath);
            if (dim.getHeight() > 0 && dim.getWidth() > 0) {
                outputValue = "<img " + getBaseAttributes(url, dim) + getAltAttribute(node) + getOtherAttributes() + " />";
            } else {
                log.warn("Found odd dimension " + dim);
                outputValue = "<img " + getSrcAttribute(url) + getAltAttribute(node) + getOtherAttributes() + " />";
            }
        }
        }
        return outputValue;
    }
    
    public String getSrcAttribute(String url) throws JspTagException {
        return " src=\"" + url + "\"";
    }

    public String getBaseAttributes(String url, Dimension dim) throws JspTagException {
        return getSrcAttribute(url) + " height=\"" + dim.getHeight() + "\" width=\"" + dim.getWidth() + "\"";
    }

    public String getAltAttribute(Node node) throws JspTagException {
        String alt = null;
        if (node.getNodeManager().hasField("alt")) {
            alt = org.mmbase.util.transformers.Xml.XMLAttributeEscape(node.getStringValue("alt"));
        }
        if ((alt == null || "".equals(alt)) && node.getNodeManager().hasField("title")) {
            alt = org.mmbase.util.transformers.Xml.XMLAttributeEscape(node.getStringValue("title"));
        } 
        if ((alt == null || "".equals(alt)) && node.getNodeManager().hasField("name")) {
            alt = org.mmbase.util.transformers.Xml.XMLAttributeEscape(node.getStringValue("name"));
        }
        if ((alt == null || "".equals(alt)) && node.getNodeManager().hasField("description")) {
            alt = org.mmbase.util.transformers.Xml.XMLAttributeEscape(node.getStringValue("description"));
        }

        
        return (alt == null ? "alt=\"\"" : " alt=\"" + alt + "\" title=\"" + alt + "\"");
    }

    protected String getOtherAttributes() throws JspTagException {
        StringBuffer attributes = new StringBuffer();
        attributes.append((styleClass != Attribute.NULL) ? (" class=\"" + styleClass.getString(this) + "\"") : "");
        attributes.append((style != Attribute.NULL) ? (" style=\"" + style.getString(this) + "\"") : "");
        attributes.append((align != Attribute.NULL) ? (" align=\"" + align.getString(this) + "\"") : "");
        attributes.append((border != Attribute.NULL) ? (" border=\"" + border.getString(this) + "\"") : "");
        attributes.append((hspace != Attribute.NULL) ? (" hspace=\"" + hspace.getString(this) + "\"") : "");
        attributes.append((vspace != Attribute.NULL) ? (" vspace=\"" + vspace.getString(this) + "\"") : "");
        return attributes.toString();
    }
    
    public Dimension getDimension(Node node, String template) {
        return new LazyDimension(node, template);
    }

    /**
     * Get template to manipulate image
     * @param node - image node
     * @param t - custom template string
     * @param widthTemplate - width of image, which is used when custom is not provided
     * @param heightTemplate - height of image, which is used when custom is not provided
     * @param cropTemplate - crop the image. values are 'begin', 'middle' and 'end'.
     * @return template for image
     */
    public String getTemplate(Node node, String t, int widthTemplate, int heightTemplate, String cropTemplate) {
        if (t == null || t.length() == 0) {
            if ((widthTemplate <= 0) && (heightTemplate <= 0)) {
                t = "";
            }
            else {
                if (cropTemplate != null && cropTemplate.length() != 0) {
                    t = getCropTemplate(node, widthTemplate, heightTemplate, cropTemplate);
                }
                else {
                    t = getResizeTemplate(node, widthTemplate, heightTemplate);
                }
            }
        }
        boolean asis = "true".equals(pageContext.getServletContext().getInitParameter("mmbase.taglib.image.format.asis"));
        if (asis) {
            if (t.length() > 0) {
                t = t + "+f(asis)";
            } else {
                t = "f(asis)";
            }
        }
        return t;
    }

    /**
     * Returns the crop template string to be used by img servlet
     * @param node - get template for this node
     * @param width - template width
     * @param height - template height
     * @return the crop template
     */
    public String getCropTemplate(Node node, int width, int height, String cropTemplate) {
        Dimension imageDimension = getDimension(node, null);
        int imageWidth = imageDimension.getWidth();
        int imageHeight = imageDimension.getHeight();
        int newWidth = width > 0 ? width : imageWidth;
        int newHeight = height > 0 ? height : imageHeight;

        // define orientation of images
        StringBuffer template = new StringBuffer();
        float horizontalMultiplier = (float) newWidth / (float) imageWidth;
        float verticalMultiplier = (float) newHeight / (float) imageHeight;
        int tempWidth = (int) (imageWidth * verticalMultiplier);
        int tempHeight = (int) (imageHeight * horizontalMultiplier);
        int xOffset = 0;
        int yOffset = 0;

        if (horizontalMultiplier == verticalMultiplier) {
            // only scaling
            template.append("s(").append(width).append(")");
        }
        else {
            if (horizontalMultiplier > verticalMultiplier) {
                // scale horizontal, crop vertical
                if (cropTemplate.equals(CROP_END)) {
                    yOffset = 0;
                }
                else {
                    if (cropTemplate.equals(CROP_BEGIN)) {
                        yOffset = (tempHeight - newHeight);
                    }
                    else {
                        // CROP_MIDDLE
                        yOffset = (tempHeight - newHeight) / 2;
                    }
                }
                template.append("s(").append(newWidth).append(")");
                template.append("+part(").append(xOffset).append(",").append(yOffset).append(",");
                template.append(xOffset + newWidth).append(",").append(yOffset + newHeight).append(")");
            }
            else {
                // scale vertical, crop horizontal
                if (cropTemplate.equals(CROP_END)) {
                    xOffset = 0;
                }
                else {
                    if (cropTemplate.equals(CROP_BEGIN)) {
                        xOffset = (tempWidth - newWidth);
                    }
                    else {
                        // CROP_MIDDLE
                        xOffset = (tempWidth - newWidth) / 2;
                    }
                }
                template.append("s(x").append(newHeight).append(")");
                template.append("+part(").append(xOffset).append(",").append(yOffset).append(",");
                template.append(xOffset + newWidth).append(",").append(yOffset + newHeight).append(")");
            }
        }
        log.debug(template.toString());
        return template.toString();
    }

    /**
     * Returns the resize template string to be used by img servlet without cropping
     * @param node - get template for this node
     * @param width - template width
     * @param height - template height
     * @return the resize template
     */
    public String getResizeTemplate(Node node, int width, int height) {
        Dimension imageDimension = getDimension(node, null);
        int imageWidth = imageDimension.getWidth();
        int imageHeight = imageDimension.getHeight();
        int newWidth = width > 0 ? width : imageWidth;
        int newHeight = height > 0 ? height : imageHeight;

        // define orientation of images
        StringBuffer template = new StringBuffer();
        float horizontalMultiplier = (float) newWidth / (float) imageWidth;
        float verticalMultiplier = (float) newHeight / (float) imageHeight;

        if (horizontalMultiplier <= verticalMultiplier) {
            // scale horizontal
            template.append("+s(").append(newWidth).append(")");
        }
        else {
            // scale vertical
            template.append("+s(x").append(newHeight).append(")");
        }

        log.debug(template.toString());
        return template.toString();
    }

}

