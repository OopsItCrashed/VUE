package tufts.vue;

import java.util.*;

import tufts.Util;
import static tufts.Util.*;
import tufts.vue.*;
import tufts.vue.gui.GUI;
import java.net.*;
import java.awt.Image;
import java.io.*;
import java.util.regex.*;

public class WormholeResource extends URLResource {
	/** See tufts.vue.URLResource - reimplementation of private member */
	private static final org.apache.log4j.Logger Log = org.apache.log4j.Logger.getLogger(URLResource.class);
	/** See tufts.vue.URLResource - reimplementation of private member */
    private static final String IMAGE_KEY = HIDDEN_PREFIX + "Image";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String THUMB_KEY = HIDDEN_PREFIX + "Thumb";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String USER_URL = "URL";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String USER_FILE = "File";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String USER_DIRECTORY = "Directory";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String USER_FULL_FILE = RUNTIME_PREFIX + "Full File";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String FILE_RELATIVE = HIDDEN_PREFIX + "file.relative";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String FILE_RELATIVE_OLD = "file.relative";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String FILE_CANONICAL = HIDDEN_PREFIX + "file.canonical";
    
    /**
     * The most generic version of what we refer to.  Usually set to a full URL or absolute file path.
     * Note that this may have been set on a different platform that we're currently running on,
     * so it may no longer make a valid URL or File on this platform, which is why we need
     * this generic String version of it, and why the Resource/URLResource code can be so complicated.
     */
    private String spec = SPEC_UNSET;
    /**
     * The target file in the wormhole
     */
    private String targetFilename;
    /**
     * The URI String of the component to focus on once we open the map.
     */
    private String componentURIString;
    /**
     * The URI String of the component to focus on once we open the originating map.
     */
    private String originatingComponentURIString;
    /**
     * The originating file in the wormhole
     */
    private String originatingFilename;
    
    /**
     * A default URL for this resource.  This will be used for "browse" actions, so for
     * example, it may point to any content available through a URL: an HTML page, raw image data,
     * document files, etc.
     */
    private URL mURL;
    
    /** Points to raw image data (greatest resolution available) */
    private URL mURL_ImageData;
    /** Points to raw image data for an image thumbnail  */
    private URL mURL_ThumbData;
    
    /**
     * This will be set if we point to a local file the user has control over.
     * This will not be set to point to cache files or package files.
     */
    private File mFile;
    
    /**
     * If this resource is relative to it's map, this will be set (at least by the time we're persisted)
     */
    private URI mRelativeURI;
    
    /** an optional resource title */
    private String mTitle;
    /** See tufts.vue.URLResource - reimplementation of private member */
    private boolean mRestoreUnderway = false;
    /** See tufts.vue.URLResource - reimplementation of private member */
    private ArrayList<PropertyEntry> mXMLpropertyList;  
    
    // HO 06/09/2010 BEGIN ***************
    private boolean bForeSaving = false;
    // HO 06/09/2010 END ***************
    
    /* static WormholeResource create(String spec) {
        return new WormholeResource(spec);
    }
    static WormholeResource create(URL url) {
        return new WormholeResource(url.toString());
    }
    static WormholeResource create(URI uri) {
        return new WormholeResource(uri.toString());
    }
    static WormholeResource create(File file) {
        return new WormholeResource(file);
    } */
    
    static WormholeResource create(java.net.URI mapURI, java.net.URI componentURI) {
        return new WormholeResource(mapURI, componentURI);
    }
    
    static WormholeResource create(java.net.URI mapURI, java.net.URI componentURI, java.net.URI originatingMapURI, java.net.URI originatingComponentURI, boolean bForeSaving) {
        return new WormholeResource(mapURI, componentURI, originatingMapURI, originatingComponentURI, bForeSaving);
    }    

    /** 
     * @param spec, the String holding the spec for this Wormhole resource
     * which will become the linked-to map file link
     * @param componentURIString, the URI String of the component to focus on when we open that map
     * 
     */
    private WormholeResource(String spec, String componentURIString) {
        init();
        setTargetFilename(spec);
        setComponentURIString(componentURIString);
        // HO 06/09/2010 BEGIN *************
        super.setSpec(spec);
        this.setSpec(spec);
        // HO 06/09/2010 BEGIN *************
        
    }
    
    /** 
     * @param mapURI, the map URI for this Wormhole resource
     * which will become the linked-to map file link
     * @param componentURI, the URI of the component to focus on when we open that map
     * 
     */
    private WormholeResource(URI mapURI, URI componentURI) {
        init();
        setTargetFilename(mapURI.toString());
        setComponentURIString(componentURI.toString());
        // HO 06/09/2010 BEGIN *************************
        super.setSpec(mapURI.toString());
        this.setSpec(mapURI.toString());
        //this.setSpec(mapURI.toString());
        // HO 06/09/2010 END *************************
    }
    
    /** 
     * @param mapURI, the map URI for this Wormhole resource
     * which will become the linked-to map file link
     * @param componentURI, the URI of the component to focus on when we open that map
     * 
     */
    private WormholeResource(URI mapURI, URI componentURI, URI originatingMapURI, URI originatingComponentURI, boolean bForeSaving) {
        setBForeSaving(true);
    	init();
    	setTargetFilename(mapURI.toString());
        setComponentURIString(componentURI.toString());
        // HO 06/09/2010 BEGIN *******************
        super.setSpec(mapURI.toString());
        this.setSpec(mapURI.toString());
        //this.setSpec(mapURI.toString());
        // HO 06/09/2010 BEGIN *******************
        setOriginatingComponentURIString(originatingComponentURI.toString());
        this.setOriginatingFilename(originatingMapURI.toString());
       }
    
    /** 
     * @param file, the file for this Wormhole resource
     * which will become the linked-to map file link
     * @param component, the component to focus on when we open that map
     * 
     */
    private WormholeResource(File file, URI componentURI) {
        init();
        setTargetFilename(file);
        setComponentURIString(componentURI.toString());
        // HO 06/09/2010 BEGIN ********************
        super.setSpecByFile(file);
        this.setSpecByFile(file);
        //this.setSpecByFile(file);  
        // HO 06/09/2010 END ********************
    }
    
    /** 
     * @param file, the source file for this Wormhole resource
     * which will become the linked-to map file link
     * @param componentURI, the component to focus on when we open that map
     * @param originatingFile, the originating file for this Wormhole resource
     * @param originatingComponentURI, the component to focus on when we open the
     * originating map
     * 
     */
    private WormholeResource(File file, URI componentURI,
    		File originatingFile, URI originatingComponentURI) {
        init();
        setTargetFilename(file);
        setComponentURIString(componentURI.toString());
        // HO 06/09/2010 BEGIN *****************
        super.setSpecByFile(file);
        this.setSpecByFile(file);
        //this.setSpecByFile(file);    
        // HO 06/09/2010 END *****************
        setOriginatingComponentURIString(originatingComponentURI.toString());
        setOriginatingFilename(originatingFile);
    }
    
    /**
     * @deprecated - This constructor needs to be public to support castor persistance ONLY -- it should not
     * be called directly by any code.
     */
    public WormholeResource() {
        init();
    } 
    
    private WormholeResource(String spec) {
        init();
        // HO 06/09/2010 BEGIN ****************
        super.setSpec(spec);
        this.setSpec(spec);
        //this.setSpec(spec);
        // HO 06/09/2010 END ****************
    }
    
    private WormholeResource(File file) {
        init();
        // HO 06/09/2010 BEGIN ****************
        super.setSpecByFile(file);
        this.setSpecByFile(file);
        //this.setSpecByFile(file);
        // HO 06/09/2010 END ****************
    }
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void init() {
        if (DEBUG.RESOURCE) {
            String iname = getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this));
            setDebugProperty("0INSTANCE", iname);
        }
    }  
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String FILE_DIRECTORY =   "directory";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String FILE_NORMAL =      "file";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String FILE_UNKNOWN =     "unknown";    
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void setSpecByFile(File file, Object knownType) {
        if (file == null) {
            Log.error("setSpecByFile", new IllegalArgumentException("null java.io.File"));
            return;
        }
        if (DEBUG.RESOURCE) dumpField("setSpecByFile; type=" + knownType, file);

        if (mURL != null)
            mURL = null;
        
        setFile(file, knownType);
        
        String fileSpec = null;
        try {
            fileSpec = file.getPath();
        } catch (Throwable t) { // for IOException
            Log.warn(file, t);
            fileSpec = file.getPath();
        }

        setSpec(fileSpec);
        
        if (DEBUG.RESOURCE && DEBUG.META && "/".equals(fileSpec)) {
            Util.printStackTrace("Root FileSystem Resource created from: " + Util.tags(file));
        }
        
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private long mLastModified;
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void setFile(File file, Object type) {

        if (mFile == file)
            return;
        
        if (DEBUG.RESOURCE||file==null) dumpField("setFile", file);
        mFile = file;

        if (file == null)
            return;

        if (mURL != null)
            setURL(null);

        type = setDataFile(file, type);

        if (mTitle == null) {
            // still true?: for some reason, if we don't always have a title set, tooltips break.  SMF 2008-04-13
            String name = file.getName();
            if (name.length() == 0) {
                // Files that are the root of a filesystem, such "C:\" will have an empty name
                // (Presumably also true for "/")
                setTitle(file.toString()); 
            } else {
                if (Util.isMacPlatform()) {
                    // colons in file names on Mac OS X display as '/' in the Finder
                    name = name.replace(':', '/');
                }
                setTitle(name);
            }
        }
        
        if (type == FILE_DIRECTORY) {
            
            setClientType(Resource.DIRECTORY);
            
        } else if (type == FILE_NORMAL) {
            
            setClientType(Resource.FILE);
            if (DEBUG.IO) dumpField("scanning mFile", file);
            mLastModified = file.lastModified();
            setByteSize(file.length());
            // todo: could attempt setURL(file.toURL()), but might fail for Win32 C: paths on the mac
            if (DEBUG.RESOURCE) {
                setDebugProperty("file.instance", mFile);
                setDebugProperty("file.modified", new Date(mLastModified));
            }
        }
    }   
    
    /**
     * Set the local file that refers to this resource, if there is one.
     * If mFile is set, mDataFile will always to same.  If this is a packaged
     * resource, mFile will NOT be set, but mDataFile should be set to the package file
     */
    private Object setDataFile(File file, Object type)  
    {
        // TODO performance: can skip isDirectory and exists tests if we
        // know this came from a LocalCabinet, which may speed up that
        // dog-slow code when expanding big directories.
        
        if (type == FILE_DIRECTORY || (type == FILE_UNKNOWN && file.isDirectory())) {
            if (DEBUG.RESOURCE && DEBUG.META) out("setDataFile: ignoring directory: " + file);
            return FILE_DIRECTORY;
            
        }
        
        final String path = file.toString();
        if (path.length() == 3 && Character.isLetter(path.charAt(0)) && path.endsWith(":\\")) {
            // Check for A:\, etc.
            // special case to ignore / prevent testing Windows currently in-accessable mount points
            // File.exists may take a while to time-out on these.
            if (DEBUG.Enabled) out_info("setDataFile: ignoring Win mount: " + file);
            return FILE_DIRECTORY;
        }
            
        if (type == FILE_UNKNOWN) {
            if (DEBUG.IO) out("testing " + file);
            if (!file.exists()) {
                // todo: could attempt decodings if a '%' is present
                // todo: if any SPECIAL chars present, could attempt encoding in all formats and then DECODING to at least the platform format
                out_warn(TERM_RED + "no such active data file: " + file + TERM_CLEAR);
                return FILE_UNKNOWN;
            }
        }
        
        mDataFile = file;

        if (mDataFile != mFile) {
            if (DEBUG.IO) dumpField("scanning mDataFile ", mDataFile);
            setByteSize(mDataFile.length());
            mLastModified = mDataFile.lastModified();
        }
        
        if (DEBUG.RESOURCE) {
            dumpField("setDataFile", file);
            setDebugProperty("file.data", file);
        }

        return FILE_NORMAL;
    }  
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void loadXMLProperties() 
    {
        if (mXMLpropertyList == null)
            return;
        
        for (KVEntry entry : mXMLpropertyList) {
            
            String key = (String) entry.getKey();
            final Object value = entry.getValue();

            // TODO: for older property maps (how to tell?) we want to re-sort the keys...
            // (and possible collapse the old keyname.### uniqified key names)
            // Todo: detect via content inspection: if contains a URL or Title, and they're
            // not at the top, do a sort.
            
            if (DEBUG.Enabled) {
                // todo: just check for keyname.###$ pattern, and somehow annotate new
                // MetaMaps so we only do this for the old ones
                final String lowKey = key.toLowerCase();
                if (lowKey.startsWith("subject."))
                    key = "Subject";
                else if (lowKey.startsWith("keywords."))
                    key = "Keywords";
            }
            
            try {
                
                // probably faster to do single set of hashed lookups at end:
                if (IMAGE_KEY.equals(key)) {
                    if (DEBUG.RESOURCE) dumpField("processing key", key);
                    setURL_Image((String) value);
                } else if (THUMB_KEY.equals(key)) {
                    if (DEBUG.RESOURCE) dumpField("processing key", key);
                    setURL_Thumb((String) value);
                } else {
                    addProperty(key, value);
                }
                
            } catch (Throwable t) {
                Log.error(this + "; loadXMLProperties: " + Util.tags(mXMLpropertyList), t);
            }
        }

        mXMLpropertyList = null;
    }   
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void setURL(URL url) {

        if (mURL == url)
            return;
        
        mURL = url;

        if (DEBUG.RESOURCE) {
            dumpField("setURL", url);
            setDebugProperty("URL", mURL);
        }
        
        if (url == null)
            return;

        if (mFile != null)
            setFile(null, FILE_UNKNOWN);
        
    }    
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void parseAndInit()
    {
        if (spec == SPEC_UNSET) {
        	if (targetFilename == null) {
        		Log.error(new Throwable("cannot initialize resource " + Util.tags(this) + " without a spec: " + Util.tags(spec)));
        		return;
        	} else {
        		setSpec(targetFilename);
        	}
        }
        
        if (targetFilename == null) {
            Log.error(new Throwable("cannot initialize resource " + Util.tags(this) + " without a target file: " + Util.tags(targetFilename)));
            return;
        }
        
        if (componentURIString == null) {
            Log.error(new Throwable("cannot initialize resource " + Util.tags(this) + " without a component URI: " + Util.tags(componentURIString)));
            return;
        }        

        if (isPackaged()) {
            
            setDataFile((File) getPropertyValue(PACKAGE_FILE), FILE_UNKNOWN);
            if (mFile != null)
                Log.warn("mFile != null" + this, new IllegalStateException(toString()));
            
        } else if (mFile == null && mURL == null) {
            
            File file = getLocalFileIfPresent(spec);
            if (file != null) {
                setFile(file, FILE_UNKNOWN); // actually, getLocalFileIfPresent may already know this exists (would need new type: FILE_KNOWN)
            } else {
                URL url = makeURL(spec);
                
                // a random string spec will not be a existing File, but will default to
                // create a file:RandomString URL (e.g. "file:My Computer"), so only set
                // URL here if it's a non-file:
                
                if (url != null && !"file".equals(url.getProtocol())) {
                    setURL(url);
                }
                // HO 06/09/2010 BEGIN ************************
                /* else if (url != null && "file".equals(url.getProtocol()) && (bForeSaving == true)) {
                	setURL(url);
                } */
                // HO 06/09/2010 END ************************
            }
            
        }

        if (getClientType() == Resource.NONE) {
            if (isLocalFile()) {
                if (mFile != null && mFile.isDirectory())
                    setClientType(Resource.DIRECTORY);
                else
                    setClientType(Resource.FILE);
            }
            else if (mURL != null)
                setClientType(Resource.URL);
        }

        if (getClientType() != Resource.DIRECTORY && !isImage()) {
            // once an image, always an image (cause setURL_Image may be called before setURL_Browse)

            if (mFile != null)
                setAsImage(looksLikeImageFile(mFile.getName())); // this just a minor optimization
            else
                setAsImage(looksLikeImageFile(this.spec)); // this is the default
            
            if (!isImage()) {
                // double-check the meta-data in case looksLikeImageFile didn't give us 100% accurate results
                checkForImageType();
            }
        }

        //-----------------------------------------------------------------------------
        // Set property information, mainly for the user, that will display
        // the minimum of what/where the resource is.
        //-----------------------------------------------------------------------------

        if (isLocalFile()) {
            if (mFile != null) {

                if (isRelative()) {
                    
                    setProperty(USER_FULL_FILE, mFile);
                    // handled in setRelativePath
                    
                } else {

                    setProperty(USER_FILE, mFile);
                }
            } else {
                setProperty(USER_FILE, spec);
            }
            removeProperty(USER_URL);

        } else {

            // todo: can use some of our getLocalFileIfPresent code to determine if
            // this is a valid URL v.s. a File from an unfamiliar filesystem
            
            String proto = null;
            if (mURL != null)
                proto = mURL.getProtocol();

            if (proto != null && (proto.startsWith("http") || proto.equals("ftp"))) {
                setProperty("URL", spec);
                removeProperty(USER_FILE);
            } else {
                if (DEBUG.RESOURCE) {
                    if (!isPackaged()) {
                        setDebugProperty("FileOrURL?", spec);
                        setDebugProperty("URL.proto", proto);
                    }
                }
            }
            
        }

        if (DEBUG.RESOURCE) {
            setDebugProperty("spec", spec);

            if (mTitle != null)
                setDebugProperty("title", mTitle);
            
        }
        
        if (!hasProperty(CONTENT_TYPE) && mURL != null)
            setProperty(CONTENT_TYPE, java.net.URLConnection.guessContentTypeFromName(mURL.getPath()));



        if (DEBUG.RESOURCE) {
            out(TERM_GREEN + "final---" + this + TERM_CLEAR);
        }

    }    
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void checkForImageType() {
        if (!isImage()) {
            if (hasProperty(CONTENT_TYPE)) {
                setAsImage(isImageMimeType(getProperty(CONTENT_TYPE)));
            } else {
                // TODO: on initial creation of resources with types unidentifiable from the spec,
                // this code will load CONTENT_TYPE (in getDataType), and determine isImage
                // with looksLikeImageFile, but then when saved/restored, the above case
                // will use isImageMimeType, which isn't the exact same test -- fix this.
                setAsImage(looksLikeImageFile('.' + getDataType()));
            }
        }
    }    
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private boolean isRelative() {
        return mRelativeURI != null;
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void setRelativeURI(URI relative) {
        mRelativeURI = relative;
        if (relative != null) {
            setProperty(FILE_RELATIVE, relative);
            setProperty(USER_FILE, getRelativePath());
            setProperty(USER_FULL_FILE, mFile);
        } else {
            removeProperty(FILE_RELATIVE);
            removeProperty(USER_FULL_FILE); // what if there's still a canonical difference?
            setProperty(USER_FILE, mFile);
        }
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private String getRelativePath() {
        return mRelativeURI == null ? null : mRelativeURI.getPath();
    } 
    
    /** @return a unique URI for this resource */
    private java.net.URI toAbsoluteURI() {
        if (mFile != null)
            return toCanonicalFile(mFile).toURI();
        else if (mURL != null)
            return makeURI(mURL);
        else
            return makeURI(getSpec());
    }
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private URI findRelativeURI(URI root)
    {
        final URI absURI = toAbsoluteURI();

        if (root.getScheme() == null || !root.getScheme().equals(absURI.getScheme())) {
            if (DEBUG.RESOURCE) Log.info(this + "; scheme=" + absURI.getScheme() + "; different scheme: " + root + "; can't be relative");
            return null;
        }
        
        if (!absURI.isAbsolute())
            Log.warn("findRelativeURI: non-absolute URI: " + absURI);

        if (DEBUG.RESOURCE) Resource.dumpURI(absURI, "CURRENT ABSOLUTE:");
        final URI relativeURI = root.relativize(absURI);

        if (relativeURI == absURI) {
            // oldRoot was unable to relativize absURI -- this resource
            // was not relative to it's map in it's previous incarnation.
            return null;
        }
        
        if (relativeURI != absURI) {
            if (DEBUG.RESOURCE) Resource.dumpURI(relativeURI, "RELATIVE FOUND:");
        }

        if (DEBUG.Enabled) {
            out(TERM_GREEN+"FOUND RELATIVE: " + relativeURI + TERM_CLEAR);
        } else {
            Log.info("found relative to " + root + ": " + relativeURI.getPath());
        }

        return relativeURI;

    } 
    
    /** @return a URI from a string that was known to already be properly encoded as a URI */
    private URI rebuildURI(String s) 
    {
        return URI.create(s);
    }
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private Object getBrowseReference()
    {
        if (mURL != null)
            return mURL;
        else if (mFile != null)
            return mFile;
        else if (mDataFile != null)
            return mDataFile;
        else
            return getSpec();
    }  
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static boolean isImageMimeType(final String s) {
        return s != null && s.toLowerCase().startsWith("image/");
    }

    /** See tufts.vue.URLResource - reimplementation of private member */
    private static boolean isHtmlMimeType(final String s) {
        return s != null && s.toLowerCase().startsWith("text/html");
    }

    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final String UNSET = "<unset-mimeType>";
    /** See tufts.vue.URLResource - reimplementation of private member */
    private String mimeType = UNSET;    
	
    /** Return exactly whatever we were handed at creation time.  We
     * need this because if it's a local file (file: URL or just local
     * file path name), we need whatever the local OS gave us as a
     * reference in order to give that to give back to openURL, as
     * it's the most reliable string to give back to the underlying OS
     * for opening a local file.  */
        public String getSpec() {
        return this.spec;
    }  
        
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static boolean isHTML(final Resource r) {
	    String s = r.getSpec().toLowerCase();
	
	    if (s.endsWith(".html") || s.endsWith(".htm"))
	        return true;
	
	    // todo: why .vue files reporting as text/html on MacOSX to content scraper?
	
	    return !s.endsWith(".vue")
	        && isHtmlMimeType(r.getProperty("url.contentType"))
	        //&& !isImage(r) // sometimes image files claim to be text/html
	        ;
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private URL getThumbshotURL(URL url) {
        if (true)
            // I don't think thumbshots ever generate images for paths beyond the root host:
            return makeURL(String.format("%s%s://%s/",
                                         THUMBSHOT_FETCH,
                                         url.getProtocol(),
                                         url.getHost()));
        else
            return makeURL(THUMBSHOT_FETCH + url);
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static String deco(String s) {
        return "<i><b>"+s+"</b></i>";
    }    
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void invalidateToolTip() {
        //mToolTipHTML = null;
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private void _scanForMetaData(URL _url) throws java.io.IOException {
        if (DEBUG.Enabled) System.out.println(this + " _scanForMetaData: xml props " + mXMLpropertyList);

        // TODO: split into scrapeHTTPMetaData for content type & size,
        // and scrapeHTML meta-data for title.  Tho really, we need
        // at this point to start having a whole pluggable set of content
        // meta-data scrapers.

        if (DEBUG.Enabled) System.out.println("*** Opening connection to " + _url);
        markAccessAttempt();
        
        Properties metaData = scrapeHTMLmetaData(_url.openConnection(), 2048);
        if (DEBUG.Enabled) System.out.println("*** Got meta-data " + metaData);
        markAccessSuccess();
        String title = metaData.getProperty("title");
        if (title != null && title.length() > 0) {
            setProperty("title", title);
            title = title.replace('\n', ' ').trim();
            setTitle(title);
        }
        try {
            setByteSize(Integer.parseInt((String) getProperty("contentLength")));
        } catch (Exception e) {}
    } 
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final Pattern HTML_Title_Regex =
        Pattern.compile(".*<\\s*title[^>]*>\\s*([^<]+)", // hacked for lang=he constructs, but too broad
                        Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

    /** See tufts.vue.URLResource - reimplementation of private member */
    private static final Pattern Content_Charset_Regex =
        Pattern.compile(".*charset\\s*=\\s*([^\">\\s]+)",
                        Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);    
    
    /** See tufts.vue.URLResource - reimplementation of private member */
    private Properties scrapeHTMLmetaData(URLConnection connection, int maxSearchBytes)
    throws java.io.IOException
{
    Properties metaData = new Properties();
    
    InputStream byteStream = connection.getInputStream();

    if (DEBUG.DND && DEBUG.META) {
        System.err.println("Getting headers from " + connection);
        System.err.println("Headers: " + connection.getHeaderFields());
    }
    
    // note: be sure to call getContentType and don't rely on getting it from the HeaderFields map,
    // as sometimes it's set by the OS for a file:/// URL when there are no header fields (no http server)
    // (actually, this is set by java via a mime type table based on file extension, or a guess based on the stream)
    if (DEBUG.DND) System.err.println("*** getting contentType & encoding...");
    final String contentType = connection.getContentType();
    final String contentEncoding = connection.getContentEncoding();
    final int contentLength = connection.getContentLength();
    
    if (DEBUG.DND) System.err.println("*** contentType [" + contentType + "]");
    if (DEBUG.DND) System.err.println("*** contentEncoding [" + contentEncoding + "]");
    if (DEBUG.DND) System.err.println("*** contentLength [" + contentLength + "]");
    
    setProperty("url.contentType", contentType);
    setProperty("url.contentEncoding", contentEncoding);
    if (contentLength >= 0)
        setProperty("url.contentLength", contentLength);

    if (!isHTML()) { // we only currently handle HTML
        if (DEBUG.Enabled) System.err.println("*** contentType [" + contentType + "] not HTML; skipping title extraction");
        return metaData;
    }
    
    if (DEBUG.DND) System.err.println("*** scanning for HTML meta-data...");

    try {
        final BufferedInputStream bufStream = new BufferedInputStream(byteStream, maxSearchBytes);
        bufStream.mark(maxSearchBytes);

        final byte[] byteBuffer = new byte[maxSearchBytes];
        int bytesRead = 0;
        int len = 0;
        // BufferedInputStream still won't read thru a block, so we need to allow
        // a few reads here to get thru a couple of blocks, so we can get up to
        // our maxbytes (e.g., a common return chunk count is 1448 bytes, presumably related to the MTU)
        do {
            int max = maxSearchBytes - bytesRead;
            len = bufStream.read(byteBuffer, bytesRead, max);
            System.out.println("*** read " + len);
            if (len > 0)
                bytesRead += len;
            else if (len < 0)
                break;
        } while (len > 0 && bytesRead < maxSearchBytes);
        if (DEBUG.DND) System.out.println("*** Got total chars: " + bytesRead);
        String html = new String(byteBuffer, 0, bytesRead);
        if (DEBUG.DND && DEBUG.META) System.out.println("*** HTML-STRING[" + html + "]");

        // first, look for a content encoding, so we can search for and get the title
        // on a properly encoded character stream

        String charset = null;

        Matcher cm = Content_Charset_Regex.matcher(html);
        if (cm.lookingAt()) {
            charset = cm.group(1);
            if (DEBUG.DND) System.err.println("*** found HTML specified charset ["+charset+"]");
            setProperty("charset", charset);
        }

        if (charset == null && contentEncoding != null) {
            if (DEBUG.DND||true) System.err.println("*** no charset found: using contentEncoding charset " + contentEncoding);
            charset = contentEncoding;
        }
        
        final String decodedHTML;
        
        if (charset != null) {
            bufStream.reset();
            InputStreamReader decodedStream = new InputStreamReader(bufStream, charset);
            if (true||DEBUG.DND) System.out.println("*** decoding bytes into characters with official encoding " + decodedStream.getEncoding());
            setProperty("contentEncoding", decodedStream.getEncoding());
            char[] decoded = new char[bytesRead];
            int decodedChars = decodedStream.read(decoded);
            decodedStream.close();
            if (true||DEBUG.DND) System.err.println("*** " + decodedChars + " characters decoded using " + charset);
            decodedHTML = new String(decoded, 0, decodedChars);
        } else
            decodedHTML = html; // we'll just have to go with the default platform charset...
        
        // these needed to be left open till the decodedStream was done, which
        // although it should never need to read beyond what's already buffered,
        // some internal java code has checks that make sure the underlying stream
        // isn't closed, even it it isn't used.
        byteStream.close();
        bufStream.close();
        
        Matcher m = HTML_Title_Regex.matcher(decodedHTML);
        if (m.lookingAt()) {
            String title = m.group(1);
            if (true||DEBUG.DND) System.err.println("*** found title ["+title+"]");
            metaData.put("title", title.trim());
        }

    } catch (Throwable e) {
        System.err.println("scrapeHTMLmetaData: " + e);
        if (DEBUG.DND) e.printStackTrace();
    }

    if (DEBUG.DND || DEBUG.Enabled) System.err.println("*** scrapeHTMLmetaData returning [" + metaData + "]");
    return metaData;
}
    
    // HO 03/09/2010 BEGIN ****************
    public String getSystemSpec() {
    	Object contentRef = getBrowseReference();
    	String systemSpec = contentRef.toString();
    	return systemSpec;
    }
    // HO 03/09/2010 END ****************

    
    /**
     * reimplementation of URLResource.displayContent()
     * This one, after opening a Map, also has to find the target
     * component and focus on that
     */
    public void displayContent() {
        final Object contentRef = getBrowseReference();

        out("displayContent: " + Util.tags(contentRef));

        final String systemSpec = contentRef.toString();
        
        try {
            markAccessAttempt();
            VueUtil.openURL(systemSpec);
    		Collection<LWMap> coll = VUE.getAllMaps();
    		for (LWMap map: coll) {
    			LWComponent theComponent = map.findChildByURIString(componentURIString);
    			if (theComponent != null) {
    				//theComponent.setSelected(true);
    				int x = (int)theComponent.getX();
    				int y = (int)theComponent.getY();
    				VUE.getActiveViewer().screenToFocalPoint(x, y);
    			}
    		}
            //LWComponent theComponent = VUE.getActiveMap().findChildByURIString(componentURIString);
            // To set the focus on the target node
            //VUE.getActiveViewer().switchFocal(theComponent);
            //VUE.setActive(LWComponent.class, this, theComponent);
            //theComponent.setSelected(true);
            //VUE.getActiveViewer().getSelection().setTo(theComponent);

            // access successful is not currently very meaningful,
            // as we don't know if the openURL failed or not.
            markAccessSuccess();
        } catch (Throwable t) {
            Log.error(systemSpec + "; " + t);
        }

        tufts.vue.gui.VueFrame.setLastOpenedResource(this);
    }
    
    /**
     * @param targetFile, the target file in this wormhole.
     */
    public void setTargetFilename(File targetFile) {
    	targetFilename = targetFile.getAbsolutePath();
    }
    
    /**
     * @param targetFilename, the target file in this wormhole.
     */
    public void setTargetFilename(String theTargetFilename) {
    	targetFilename = theTargetFilename;
    }    
    
    /**
     * @return targetFilename, the absolute path of the target file in
     * this wormhole.
     */
    public String getTargetFilename() {
    	return targetFilename;
    }
    
    /**
     * @param theComponentURIString, the URI String for the LWComponent we want to focus on
     * once we've opened the map.
     */
    public void setComponentURIString(String theComponentURIString) {
    	componentURIString = theComponentURIString;
    }
    
    /**
     * @return componentURIString, the URI String for the LWComponent we want to focus on
     * once we've opened the map.
     */
    public String getComponentURIString() {
    	return componentURIString;
    }
    
    /**
     * @param originatingFile, the originating file in this wormhole.
     */
    public void setOriginatingFilename(File originatingFile) {
    	originatingFilename = originatingFile.getAbsolutePath();
    }
    
    /**
     * @param originatingFilename, the originating file in this wormhole.
     */
    public void setOriginatingFilename(String theOriginatingFilename) {
    	originatingFilename = theOriginatingFilename;
    }    
    
    /**
     * @return originatingFilename, the absolute path of the originating file in
     * this wormhole.
     */
    public String getOriginatingFilename() {
    	return originatingFilename;
    }
    
    /**
     * @param theComponentURIString, the URI String for the LWComponent we want to focus on
     * once we've opened the originating map.
     */
    public void setOriginatingComponentURIString(String theComponentURIString) {
    	originatingComponentURIString = theComponentURIString;
    }
    
    /**
     * @return originatingComponentURIString, the URI String for the LWComponent we want to focus on
     * once we've opened the map.
     */
    public String getOriginatingComponentURIString() {
    	return originatingComponentURIString;
    }
    
    public void setSpec(final String newSpec) {

        if ((DEBUG.RESOURCE||DEBUG.WORK) && this.spec != SPEC_UNSET) {
            out("setSpec; already set: replacing "
                + Util.tags(this.spec) + " " + Util.tag(spec)
                + " with " + Util.tags(newSpec) + " " + Util.tag(newSpec));
            //Log.warn(this + "; setSpec multiple calls", new IllegalStateException("setSpec: multiple calls; resources are atomic"));
            //return;
        }

        if (DEBUG.RESOURCE) dumpField(TERM_CYAN + "setSpec------------------------" + TERM_CLEAR, newSpec);
        
        if (newSpec == null)
            throw new IllegalArgumentException(Util.tags(this) + "; setSpec: null value");

        if (SPEC_UNSET.equals(newSpec)) {
            this.spec = SPEC_UNSET;
            return;
        }

        this.spec = newSpec;

        reset();
        
        if (!mRestoreUnderway)
            parseAndInit();

        //if (DEBUG.RESOURCE) out("setSpec: complete; " + this);
    }    
    
    public void setBForeSaving(boolean b) {
    	bForeSaving = b;
    }
    
    public boolean getBForeSaving() {
    	return bForeSaving;
    }
    
    @Override
    protected void initFinal(Object context) 
    {
        if (DEBUG.RESOURCE) out("initFinal in " + context);
        parseAndInit();
    }
    
    @Override
    public void restoreRelativeTo(URI root) 
    {
        // Even if the existing original resource exists, we always
        // choose the relative / "local" version, if it can be found.
    	// erm, no we don't.... we want the opposite where 
    	// a WormholeResource is concerned.
    	// So I guess this is to find the relative / "local" version.
    	
    	try {
    	
        // now we get where the file is relative to us now? I think
		String relative = getProperty(FILE_RELATIVE_OLD);
        if (relative == null) {
            relative = getProperty(FILE_RELATIVE);
            if (relative == null) {
                // attempt to find us in case we're relative anyway:
                //recordRelativeTo(root); 
                return; // nothing to do
            }
            
        } else {
            removeProperty(FILE_RELATIVE_OLD);
            setProperty(FILE_RELATIVE, relative);
        }
        
        System.out.println("Component URI string is: " + getComponentURIString());
    	
    	// now we need to know what the spec is currently
    	// to see if it needs to be reset or not.
    	String currentSpec = this.getTargetFilename();
    	/* File currentRoot = null;
    	URI theCurrentRoot = null;
    	if ((currentSpec != null) && (currentSpec != "")) { 	
    		File curFile = new File(currentSpec);
    		currentRoot = curFile.getParentFile();
			if (currentRoot != null) {
				theCurrentRoot = currentRoot.toURI();
				if (theCurrentRoot == null)
					return;
			}
    	} else {
    		return;
    	} */
    	
    	if ((currentSpec == null) || (currentSpec == ""))
    		return;

        final URI relativeURI = rebuildURI(relative);
        //final URI absoluteURI = theCurrentRoot.resolve(relativeURI);
        final URI absoluteURI = new URI(currentSpec);
        

        if (DEBUG.RESOURCE) {
            System.out.print(TERM_PURPLE);
            Resource.dumpURI(absoluteURI, "resolved absolute:");
            Resource.dumpURI(relativeURI, "from relative:");
            System.out.print(TERM_CLEAR);
        }
        
        if (absoluteURI != null) {

            final File file = new File(absoluteURI);

            if (file.canRead()) {
                // only change the spec if we can actually find the file (todo: test Vista -- does canRead work?)
                if (DEBUG.RESOURCE) setDebugProperty("relative URI", relativeURI);
                Log.info(TERM_PURPLE + "resolved " + relativeURI.getPath() + " to: " + file + TERM_CLEAR);
                setRelativeURI(relativeURI);
                setSpecByFile(file);
            } else {
                out_warn(TERM_RED + "can't find data relative to " + root + " at " + relative + "; can't read " + file + TERM_CLEAR);
                // todo: should probably delete the relative property key/value at this point
            }
        } else {
            out_error("failed to find relative " + relative + "; in " + root + " for " + this);
        } 
    	} catch (URISyntaxException e) {
    		e.printStackTrace();
    	}
    }
    

}