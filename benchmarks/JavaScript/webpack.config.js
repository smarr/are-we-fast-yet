const path = require('path');

module.exports = {
  mode: 'production',
  optimization: {
    minimize: false
  },
  entry: './harness.js',
  output: {
    filename: 'harness.js',
    path: path.resolve(__dirname, 'dist'),
  },
};
