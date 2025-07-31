import { Tabs } from 'expo-router';
import React from 'react';

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: '#9945FF',
        headerShown: false,
      }}>
      <Tabs.Screen name="index" options={{ title: 'Discover' }} />
      <Tabs.Screen name="superstar" options={{ title: 'Superstar' }} />
      <Tabs.Screen name="rankings" options={{ title: 'Rankings' }} />
      <Tabs.Screen name="activities" options={{ title: 'Activities' }} />
      <Tabs.Screen name="profile" options={{ title: 'Profile' }} />
    </Tabs>
  );
}