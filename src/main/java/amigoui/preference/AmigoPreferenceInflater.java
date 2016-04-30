package amigoui.preference;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import com.amigoui.internal.util.AmigoXmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class AmigoPreferenceInflater extends AmigoGenericInflater<AmigoPreference, AmigoPreferenceGroup> {
    private static final String EXTRA_TAG_NAME = "extra";
    private static final String INTENT_TAG_NAME = "intent";
    private static final String TAG = "PreferenceInflater";
    private AmigoPreferenceManager mPreferenceManager;

    public AmigoPreferenceInflater(Context context, AmigoPreferenceManager preferenceManager) {
        super(context);
        init(preferenceManager);
    }

    AmigoPreferenceInflater(AmigoGenericInflater<AmigoPreference, AmigoPreferenceGroup> original, AmigoPreferenceManager preferenceManager, Context newContext) {
        super(original, newContext);
        init(preferenceManager);
    }

    public AmigoGenericInflater<AmigoPreference, AmigoPreferenceGroup> cloneInContext(Context newContext) {
        return new AmigoPreferenceInflater(this, this.mPreferenceManager, newContext);
    }

    private void init(AmigoPreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        setDefaultPackage("amigoui.preference.");
    }

    protected boolean onCreateCustomFromTag(XmlPullParser parser, AmigoPreference parentPreference, AttributeSet attrs) throws XmlPullParserException {
        XmlPullParserException ex;
        String tag = parser.getName();
        if (tag.equals(INTENT_TAG_NAME)) {
            try {
                Intent intent = Intent.parseIntent(getContext().getResources(), parser, attrs);
                if (intent == null) {
                    return true;
                }
                parentPreference.setIntent(intent);
                return true;
            } catch (IOException e) {
                ex = new XmlPullParserException("Error parsing preference");
                ex.initCause(e);
                throw ex;
            }
        } else if (!tag.equals(EXTRA_TAG_NAME)) {
            return false;
        } else {
            getContext().getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs, parentPreference.getExtras());
            try {
                AmigoXmlUtils.skipCurrentTag(parser);
                return true;
            } catch (IOException e2) {
                ex = new XmlPullParserException("Error parsing preference");
                ex.initCause(e2);
                throw ex;
            }
        }
    }

    protected AmigoPreferenceGroup onMergeRoots(AmigoPreferenceGroup givenRoot, boolean attachToGivenRoot, AmigoPreferenceGroup xmlRoot) {
        if (givenRoot != null) {
            return givenRoot;
        }
        xmlRoot.onAttachedToHierarchy(this.mPreferenceManager);
        return xmlRoot;
    }
}
