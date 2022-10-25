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
    chunkFormat: 'commonjs'
  },
  module: {
    rules: [
      {
        test: /\.m?js$/,
        exclude: /(node_modules|bower_components)/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      }
    ]
  },
  target: 'es5'
};
