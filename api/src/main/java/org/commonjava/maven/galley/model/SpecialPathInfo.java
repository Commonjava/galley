/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.model;

/**
 * Created by jdcasey on 1/27/16.
 */
public class SpecialPathInfo
{
    private SpecialPathMatcher matcher;

    private boolean retrievable = true;

    private boolean publishable = true;

    private boolean listable = true;

    private boolean decoratable = true;

    private boolean storable = true;

    private boolean deletable = true;

    private boolean metadata = false;

    /**
     * Flag reflecting the fact that this content should not be returned alone when in a group but rather should be
     * merged using all available sources.
     */
    private boolean mergable = false;


    public static Builder from( final SpecialPathMatcher matcher )
    {
        return new Builder( matcher );
    }


    protected SpecialPathInfo( final SpecialPathMatcher matcher, final boolean retrievable, final boolean publishable,
                               final boolean listable, final boolean decoratable, final boolean storable,
                               final boolean deletable, final boolean metadata, final boolean mergable )
    {
        this.matcher = matcher;
        this.retrievable = retrievable;
        this.publishable = publishable;
        this.listable = listable;
        this.decoratable = decoratable;
        this.storable = storable;
        this.deletable = deletable;
        this.metadata = metadata;
        this.mergable = mergable;
    }

    protected SpecialPathInfo( final SpecialPathMatcher matcher )
    {
        this.matcher = matcher;
    }


    public SpecialPathMatcher getMatcher()
    {
        return matcher;
    }

    protected void setMatcher( final SpecialPathMatcher matcher )
    {
        this.matcher = matcher;
    }

    public boolean isRetrievable()
    {
        return retrievable;
    }

    protected void setRetrievable( final boolean retrievable )
    {
        this.retrievable = retrievable;
    }

    public boolean isPublishable()
    {
        return publishable;
    }

    protected void setPublishable( final boolean publishable )
    {
        this.publishable = publishable;
    }

    public boolean isListable()
    {
        return listable;
    }

    protected void setListable( final boolean listable )
    {
        this.listable = listable;
    }

    public boolean isDecoratable()
    {
        return decoratable;
    }

    protected void setDecoratable( final boolean decoratable )
    {
        this.decoratable = decoratable;
    }

    protected void setStorable( final boolean storable )
    {
        this.storable = storable;
    }

    public boolean isStorable()
    {
        return storable;
    }

    public boolean isDeletable()
    {
        return deletable;
    }

    protected void setDeletable( final boolean deletable )
    {
        this.deletable = deletable;
    }

    public boolean isMetadata()
    {
        return metadata;
    }

    public void setMetadata( final boolean metadata )
    {
        this.metadata = metadata;
    }

    public boolean isMergable()
    {
        return mergable;
    }

    protected void setMergable( final boolean mergable )
    {
        this.mergable = mergable;
    }


    public static class Builder
    {
        private SpecialPathInfo info;

        protected Builder( final SpecialPathMatcher matcher )
        {
            this.info = new SpecialPathInfo( matcher );
        }

        public SpecialPathInfo build()
        {
            SpecialPathInfo result = info;
            info = new SpecialPathInfo( result.getMatcher() );

            return result;
        }

        public boolean isRetrievable()
        {
            return info.isRetrievable();
        }

        public boolean isDecoratable()
        {
            return info.isDecoratable();
        }

        public boolean isListable()
        {
            return info.isListable();
        }

        public boolean isPublishable()
        {
            return info.isPublishable();
        }

        public SpecialPathMatcher getMatcher()
        {
            return info.getMatcher();
        }

        public Builder setRetrievable( final boolean retrievable )
        {
            info.setRetrievable( retrievable );
            return this;
        }

        public Builder setListable( final boolean listable )
        {
            info.setListable( listable );
            return this;
        }

        public Builder setDecoratable( final boolean decoratable )
        {
            info.setDecoratable( decoratable );
            return this;
        }

        public Builder setPublishable( final boolean publishable )
        {
            info.setPublishable( publishable );
            return this;
        }

        public Builder setStorable( final boolean storable )
        {
            info.setStorable( storable );
            return this;
        }

        public boolean isStorable()
        {
            return info.isStorable();
        }

        public Builder setDeletable( final boolean deletable )
        {
            info.setDeletable( deletable );
            return this;
        }

        public boolean isDeletable()
        {
            return info.isDeletable();
        }

        public Builder setMetadata( final boolean metadata )
        {
            info.setMetadata( metadata );
            return this;
        }

        public boolean isMetadata()
        {
            return info.isMetadata();
        }

        public Builder setMergable( final boolean mergable )
        {
            info.setMergable( mergable );
            return this;
        }

        public boolean isMergable()
        {
            return info.mergable;
        }
    }

}
