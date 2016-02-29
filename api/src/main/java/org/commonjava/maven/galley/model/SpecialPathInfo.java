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

    public static Builder from( SpecialPathMatcher matcher )
    {
        return new Builder( matcher );
    }

    protected SpecialPathInfo( SpecialPathMatcher matcher, boolean retrievable, boolean publishable, boolean listable,
                            boolean decoratable, boolean storable, boolean deletable )
    {
        this.matcher = matcher;
        this.retrievable = retrievable;
        this.publishable = publishable;
        this.listable = listable;
        this.decoratable = decoratable;
        this.storable = storable;
        this.deletable = deletable;
    }

    protected SpecialPathInfo( SpecialPathMatcher matcher )
    {
        this.matcher = matcher;
    }

    public SpecialPathMatcher getMatcher()
    {
        return matcher;
    }

    protected void setMatcher( SpecialPathMatcher matcher )
    {
        this.matcher = matcher;
    }

    public boolean isRetrievable()
    {
        return retrievable;
    }

    protected void setRetrievable( boolean retrievable )
    {
        this.retrievable = retrievable;
    }

    public boolean isPublishable()
    {
        return publishable;
    }

    protected void setPublishable( boolean publishable )
    {
        this.publishable = publishable;
    }

    public boolean isListable()
    {
        return listable;
    }

    protected void setListable( boolean listable )
    {
        this.listable = listable;
    }

    public boolean isDecoratable()
    {
        return decoratable;
    }

    protected void setDecoratable( boolean decoratable )
    {
        this.decoratable = decoratable;
    }

    protected void setStorable( boolean storable )
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

    protected void setDeletable( boolean deletable )
    {
        this.deletable = deletable;
    }

    public static class Builder
    {
        private SpecialPathInfo info;

        protected Builder( SpecialPathMatcher matcher )
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

        public Builder setRetrievable( boolean retrievable )
        {
            info.setRetrievable( retrievable );
            return this;
        }

        public Builder setListable( boolean listable )
        {
            info.setListable( listable );
            return this;
        }

        public Builder setDecoratable( boolean decoratable )
        {
            info.setDecoratable( decoratable );
            return this;
        }

        public Builder setPublishable( boolean publishable )
        {
            info.setPublishable( publishable );
            return this;
        }

        public Builder setStorable( boolean storable )
        {
            info.setStorable( storable );
            return this;
        }

        public boolean isStorable()
        {
            return info.isStorable();
        }

        public Builder setDeletable( boolean deletable )
        {
            info.setDeletable( deletable );
            return this;
        }

        public boolean isDeletable()
        {
            return info.isDeletable();
        }
    }

}
