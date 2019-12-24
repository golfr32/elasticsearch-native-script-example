/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.examples.nativescript.script;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptException;
import org.elasticsearch.search.lookup.IndexField;
import org.elasticsearch.search.lookup.IndexFieldTerm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Script that scores documents with cosine similarity, see Manning et al.,
 * "Information Retrieval", Chapter 6, Eq. 6.12 (link:
 * http://nlp.stanford.edu/IR-book/). This implementation only scores a list of
 * terms on one field.
 */
public class JaccardSimilarityScoreScript extends AbstractSearchScript {

    // the field containing the terms that should be scored, must be initialized
    // in constructor from parameters.
    String field = null;
    // terms that are used for scoring, must be unique
    ArrayList<String> terms = null;
    // weights, in case we want to put emphasis on a specific term. In the most
    // simple case, 1.0 for every term.
    ArrayList<Double> weights = null;

    final static public String SCRIPT_NAME = "jaccard_sim_script_score";

    private final static Logger logger = LogManager.getLogger(JaccardSimilarityScoreScript.class);

    /**
     * Factory that is registered in
     * {@link org.elasticsearch.examples.nativescript.plugin.NativeScriptExamplesPlugin#onModule(org.elasticsearch.script.ScriptModule)}
     * method when the plugin is loaded.
     */
    public static class Factory implements NativeScriptFactory {

        /**
         * This method is called for every search on every shard.
         *
         * @param params
         *            list of script parameters passed with the query
         * @return new native script
         */
        @Override
        public ExecutableScript newScript(@Nullable Map<String, Object> params) throws ScriptException {
            return new JaccardSimilarityScoreScript(params);
        }

        @Override
        public String getName() {
            return JaccardSimilarityScoreScript.SCRIPT_NAME;
        }


        /**
         * Indicates if document scores may be needed by the produced scripts.
         *
         * @return {@code true} if scores are needed.
         */
        @Override
        public boolean needsScores() {
            return false;
        }
    }

    /**
     * @param params
     *            terms that a scored are placed in this parameter. Initialize
     *            them here.
     * @throws ScriptException
     */
    private JaccardSimilarityScoreScript(Map<String, Object> params) throws ScriptException {
        params.entrySet();
        // get the terms
        terms = (ArrayList<String>) params.get("terms");

        weights = (ArrayList<Double>) params.get("weights");
        // get the field
        field = (String) params.get("field");
        //logger.info("calculating the similarity terms size:"+ terms.size()+" field:"+ field );
        if (field == null || terms == null || weights == null) {
            throw new ScriptException(
                "cannot initialize " + JaccardSimilarityScoreScript.SCRIPT_NAME + ": field, terms or weights parameter missing!", null, Collections.emptyList(),
                "exception on unknown var", JaccardSimilarityScoreScript.SCRIPT_NAME
            );
        }
        if (weights.size() != terms.size()) {
            throw new ScriptException(
                "cannot initialize " + JaccardSimilarityScoreScript.SCRIPT_NAME + ": terms and weights array must have same length!", null, Collections.emptyList(),
                "exception on unknown var", JaccardSimilarityScoreScript.SCRIPT_NAME
            );
        }
    }

    private Double getPositionFromTerms(String key){
        if( !key.isEmpty() ) {
            for (int i = 0; i < terms.size(); i++) {
                if (terms.get(i).equals(key))
                    return weights.get(i);
            }
        }
        return 0.0;
    }

    @Override
    public Object run() {
        try {
            int k = 0;
            float score = 0;
            // first, get the ShardTerms object for the field.
            IndexField indexField = this.indexLookup().get(field);
            double queryWeightSum = 0.0f;
            double docWeightSum = 0.0f;
            double scoreValue = 0.0f;


            scoreValue = (double)(score / (Math.sqrt(docWeightSum) * Math.sqrt(queryWeightSum)));
            logger.info("calculating the cosine similarity score:"+ score +" queryWeightSum:"+ queryWeightSum + " docWeightSum:"+ docWeightSum +" cosine:" + scoreValue);
            return scoreValue;
        } catch (IOException ex) {
            throw new ScriptException(
                "Could not compute cosine similarity: "+ex.getMessage(), null, Collections.emptyList(),
                "exception on unknown var", JaccardSimilarityScoreScript.SCRIPT_NAME
            );
        }
    }

}
