/**
 * TT-LIKE-APP Main Application
 * Root component with navigation and providers
 */

import React from 'react';
import { StatusBar } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import Toast from 'react-native-toast-message';

// Contexts
import { AuthProvider } from './context/AuthContext';
import { FeedProvider } from './context/FeedContext';

// Navigation
import RootNavigator from './navigation/RootNavigator';

// Styles
import { colors } from './utils/constants';

const App = () => {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AuthProvider>
          <FeedProvider>
            <NavigationContainer>
              <StatusBar 
                barStyle="light-content" 
                backgroundColor={colors.black}
              />
              <RootNavigator />
            </NavigationContainer>
            <Toast />
          </FeedProvider>
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
};

export default App;
