/*
 * Copyright 2005-2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dao.mock;

import java.util.*;

import org.unitedinternet.cosmo.dao.*;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.mock.MockGroup;
import org.unitedinternet.cosmo.model.mock.MockUser;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockUserDao implements UserDao {
    static int idseq = 0;


    @SuppressWarnings("rawtypes")
    private HashMap usernameIdx;
    @SuppressWarnings("rawtypes")
    private HashMap emailIdx;
    @SuppressWarnings("rawtypes")
    private HashMap uidIdx;
    @SuppressWarnings("rawtypes")
    private HashMap activationIdIdx;
    private HashMap<String, PasswordRecovery> passwordRecoveryIdx;

    private final HashMap groupUidIdx;
    private final HashMap groupUsernameIdx;

    private MockDaoStorage storage = null;
    
    private VersionFourGenerator idGenerator = new VersionFourGenerator();

    /**
     * Constructor.
     * @param storage The mock dao storage.
     */
    @SuppressWarnings("rawtypes")
    public MockUserDao(MockDaoStorage storage) {
        this.storage = storage;
        usernameIdx = new HashMap();
        emailIdx = new HashMap();
        uidIdx = new HashMap();
        activationIdIdx = new HashMap();
        passwordRecoveryIdx = new HashMap<String, PasswordRecovery>();

        //Indexes for groups
        groupUidIdx = new HashMap<>();
        groupUsernameIdx = new HashMap<>();


        // add overlord user
        MockUser overlord = new MockUser();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("32a8bd4d676f4fef0920c7da8db2bad7");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(true);
        overlord.setCreationDate(new Date());
        overlord.setModifiedDate(new Date());
        createUser(overlord);

        //Add overlords group
        MockGroup overlords = new MockGroup();
        overlords.setDisplayName("Overlords");
        overlords.setUsername("overlords");
        overlord.addGroup(overlords);
        createGroup(overlords);
        updateUser(overlord);
    }

    // UserDao methods

    /**
     * Gets user.
     * {@inheritDoc}
     * @param username The username.
     * @return The user.
     */
    public User getUser(String username) {
        if (username == null) {
            return null;
        }
        return (User) usernameIdx.get(username);
    }

    @Override
    public Group getGroup(String name) {
        if (name == null) {
            return null;
        }
        return (Group) groupUsernameIdx.get(name);
    }

    @Override
    public UserIterator users() {
        Iterator iterator = usernameIdx.values().iterator();
        return new UserIterator() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public User next() {
                return (User) iterator.next();
            }
        };
    }

    @Override
    public GroupIterator groups() {
        Iterator iterator = groupUsernameIdx.values().iterator();
        return new GroupIterator() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Group next() {
                return (Group) iterator.next();
            }
        };
    }

    /**
     * Gets user by uid.
     * {@inheritDoc}
     * @param uid The uid.
     * @return The user.
     */
    public User getUserByUid(String uid) {
        if (uid == null) { 
            return null;
        }
        return (User) uidIdx.get(uid);
    }

    @Override
    public Group getGroupByUid(String uid) {
        if (uid == null) {
            return null;
        }
        return (Group) groupUidIdx.get(uid);
    }

    /**
     * Gets user by activation id.
     * {@inheritDoc}
     * @param id The id.
     * @return The user.
     */
    public User getUserByActivationId(String id) {
        if (id == null) {
            return null;
        }
        return (User) activationIdIdx.get(id);
    }

    /**
     * Gets user by email.
     * {@inheritDoc}
     * @param email The email.
     * @return The user.
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return (User) emailIdx.get(email);
    }

    /**
     * Creates user.
     * {@inheritDoc}
     * @param user The user.
     * @return The user.
     */
    @SuppressWarnings("unchecked")
    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        user.setUid(idGenerator.nextStringIdentifier());
        
        // Set create/modified date, etag for User and associated subscriptions
        // and perferences.

        
        for(Preference p: user.getPreferences()) {
            p.updateTimestamp();
        }
        user.updateTimestamp();
        ((MockUser) user).validate();
        if (usernameIdx.containsKey(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        if (emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        
        usernameIdx.put(user.getUsername(), user);
        emailIdx.put(user.getEmail(), user);
        uidIdx.put(user.getUid(), user);
        activationIdIdx.put(user.getActivationId(), user);
        return user;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Group createGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("null group");
        }
        group.setUid(idGenerator.nextStringIdentifier());
        group.updateTimestamp();

        ((MockGroup) group).validateUsername();
        if (groupUsernameIdx.containsKey(group.getUsername())) {
            throw new DuplicateUsernameException(group.getUsername());
        }

        groupUsernameIdx.put(group.getUsername(), group);
        groupUidIdx.put(group.getUid(), group);
        return group;

    }

    /**
     * Updates user.
     * {@inheritDoc}
     * @param user The user.
     * @return The user.
     */
    @SuppressWarnings("unchecked")
    @Override
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }
        
        // Update modified date, etag for User and associated subscriptions
        // and preferences.

        for(Preference p: user.getPreferences()) {
            p.updateTimestamp();
        }
        user.updateTimestamp();

        ((MockUser) user).validate();
        String key = user.isUsernameChanged() ?
            user.getOldUsername() :
            user.getUsername();
        if (! usernameIdx.containsKey(key)) {
            throw new IllegalArgumentException("user not found");
        }
        if (user.isUsernameChanged() &&
            usernameIdx.containsKey(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        if (user.isEmailChanged() && emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        usernameIdx.put(user.getUsername(), user);
        if (user.isUsernameChanged()) {
            usernameIdx.remove(user.getOldUsername());
            storage.setRootUid(user.getUsername(), storage.getRootUid(user.getOldUsername()));
        }
        emailIdx.put(user.getEmail(), user);
        if (user.isEmailChanged()) {
            emailIdx.remove(user.getOldEmail());
        }
        return user;
    }

    @Override
    public Group updateGroup(Group group) {
        // Check for not null
        if (group == null) {
            throw new IllegalArgumentException("null group");
        }


        //update all timestamps
        for (Preference p: group.getPreferences()) {
            p.updateTimestamp();
        }
        group.updateTimestamp();

        ((MockGroup) group).validateUsername();

        String key = group.isUsernameChanged() ?
            group.getOldUsername() :
            group.getUsername();
        if (! groupUsernameIdx.containsKey(key)) {
            throw new IllegalArgumentException("group not found");
        }
        if (group.isUsernameChanged() &&
                groupUsernameIdx.containsKey(group.getUsername())) {
            throw new DuplicateUsernameException(group.getUsername());
        }


        groupUsernameIdx.put(group.getUsername(), group);
        if (group.isUsernameChanged()) {
            groupUsernameIdx.remove(group.getOldUsername());
            storage.setRootUid(group.getUsername(), storage.getRootUid(group.getOldUsername()));
        }
        return group;
    }

    /**
     * Removes user.
     * {@inheritDoc}
     * @param username The username.
     */
    public void removeUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (usernameIdx.containsKey(username)) {
            User user = (User) usernameIdx.get(username);
            usernameIdx.remove(username);
            emailIdx.remove(user.getEmail());
        }
    }

    @Override
    public void removeGroup(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null group name");
        }
        if (usernameIdx.containsKey(name)) {
            groupUsernameIdx.remove(name);
        }
    }

    /**
     * Removes user.
     * {@inheritDoc}
     * @param user The user.
     */
    public void removeUser(User user) {
        if (user == null) {
            return;
        }
        usernameIdx.remove(user.getUsername());
        emailIdx.remove(user.getEmail());
    }

    @Override
    public void removeGroup(Group group) {
        if (group == null) {
            return;
        }
        groupUsernameIdx.remove(group.getUsername());
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    /**
     * Creates password recovery.
     * {@inheritDoc}
     * @param passwordRecovery The password recovery.
     */
    public void createPasswordRecovery(PasswordRecovery passwordRecovery) {
        passwordRecoveryIdx.put(passwordRecovery.getKey(), passwordRecovery);
        
    }

    /**
     * Deletes passwordRecovery.
     * {@inheritDoc}
     * @param passwordRecovery Password Recovery.
     */
    public void deletePasswordRecovery(PasswordRecovery passwordRecovery) {
        passwordRecoveryIdx.remove(passwordRecovery.getKey());
        
    }

    /**
     * Gets the password Recovery.
     * {@inheritDoc}
     * @param key The key.
     * @re passwordRecovery The passwordRecovery.
     */
    public PasswordRecovery getPasswordRecovery(String key) {
        return passwordRecoveryIdx.get(key);
    }

    /**
     * Finds users by preference.
     * {@inheritDoc}
     * @param key The key.
     * @param value The value.
     * @return The users.
     */
    @SuppressWarnings("unchecked")
    public Set<User> findUsersByPreference(String key, String value) {
        HashSet<User> results = new HashSet<User>();
        for (User user : (Collection<User>) usernameIdx.values()) {
            for (Preference pref: user.getPreferences()) {
                if (pref.getKey().equals(key) && pref.getValue().equals(value)) {
                    results.add(user);
                }
            }
        }
        
        return results;
    }

}
